package com.oodt.fruitguard.core;

import javax.sound.midi.*;

/** 使用 JDK MIDI 合成器播放原创循环旋律，不依赖外部音频文件。 */
public final class MusicManager implements AutoCloseable {
    private Sequencer sequencer;
    private boolean enabled = true;

    public MusicManager() {
        try {
            sequencer = MidiSystem.getSequencer();
            if (sequencer == null) { enabled = false; return; }
            sequencer.open();
            sequencer.setSequence(buildSequence());
            sequencer.setTempoInBPM(108);
            sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
            sequencer.start();
        } catch (MidiUnavailableException | InvalidMidiDataException ignored) {
            if (sequencer != null) sequencer.close();
            sequencer = null;
            enabled = false;
        }
    }

    private Sequence buildSequence() throws InvalidMidiDataException {
        Sequence sequence = new Sequence(Sequence.PPQ, 8);
        Track melody = sequence.createTrack();
        Track bass = sequence.createTrack();
        melody.add(programChange(0, 10, 0));
        bass.add(programChange(1, 32, 0));

        int[] notes = {67, 71, 74, 71, 69, 72, 76, 72, 64, 67, 71, 74, 62, 67, 69, 71,
                72, 76, 79, 76, 74, 71, 67, 69, 71, 74, 76, 74, 71, 69, 67, 64};
        long tick = 0;
        for (int note : notes) {
            melody.add(note(ShortMessage.NOTE_ON, 0, note, 52, tick));
            melody.add(note(ShortMessage.NOTE_OFF, 0, note, 0, tick + 6));
            tick += 8;
        }

        int[] roots = {48, 45, 41, 43, 48, 45, 41, 43};
        tick = 0;
        for (int root : roots) {
            bass.add(note(ShortMessage.NOTE_ON, 1, root, 36, tick));
            bass.add(note(ShortMessage.NOTE_ON, 1, root + 7, 28, tick));
            bass.add(note(ShortMessage.NOTE_OFF, 1, root, 0, tick + 30));
            bass.add(note(ShortMessage.NOTE_OFF, 1, root + 7, 0, tick + 30));
            tick += 32;
        }
        return sequence;
    }

    private MidiEvent note(int command, int channel, int note, int velocity, long tick)
            throws InvalidMidiDataException {
        ShortMessage message = new ShortMessage();
        message.setMessage(command, channel, note, velocity);
        return new MidiEvent(message, tick);
    }

    private MidiEvent programChange(int channel, int program, long tick)
            throws InvalidMidiDataException {
        ShortMessage message = new ShortMessage();
        message.setMessage(ShortMessage.PROGRAM_CHANGE, channel, program, 0);
        return new MidiEvent(message, tick);
    }

    public boolean toggle() {
        if (!available()) { enabled = false; return false; }
        enabled = !enabled;
        if (sequencer != null) {
            if (enabled) sequencer.start();
            else sequencer.stop();
        }
        return enabled;
    }

    public boolean enabled() { return enabled; }
    public boolean available() { return sequencer != null && sequencer.isOpen(); }

    @Override
    public void close() {
        if (sequencer != null) sequencer.close();
    }
}
