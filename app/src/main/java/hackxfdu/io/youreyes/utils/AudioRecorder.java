package hackxfdu.io.youreyes.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import static hackxfdu.io.youreyes.utils.FileUtils.existsSDCard;


/**
 * A custom audio recorder that supports generating WAV file.
 * Here we use singleton pattern.
 */
public class AudioRecorder {

    public final static String AUDIO_WAV_FILENAME_DEFAULT = "eyes_record_audio.wav";

    // Buffer size
    private int bufferSizeInBytes = 0;

    // Raw audio filename
    private String rawAudioName = "";

    // Generated audio filename
    private String newAudioName = "";

    private AudioRecord audioRecord;
    private boolean isRecord = false;


    private static AudioRecorder mInstance;

    private AudioRecorder() {
    }

    public synchronized static AudioRecorder getInstance() {
        if (mInstance == null)
            mInstance = new AudioRecorder();
        return mInstance;
    }

    public int startRecordAndFile() {
        if (existsSDCard()) {
            if (isRecord) {
                return AudioErrorCode.E_STATE_RECODING;
            }
            if (audioRecord == null) {
                createAudioRecord();
            }

            audioRecord.startRecording();
            isRecord = true;
            // Start a new thread to write audio file
            new Thread(new AudioRecordThread()).start();
            return AudioErrorCode.SUCCESS;
        } else {
            return AudioErrorCode.E_NO_SDCARD;
        }

    }

    public void stopRecordAndFile() {
        close();
    }


    public long getRecordFileSize() {
        return FileUtils.getFileSize(newAudioName);
    }


    private void close() {
        if (audioRecord != null) {
            Log.v("YourEyes", "AudioRecorder::stopRecord");
            isRecord = false;
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
    }

    private void createAudioRecord() {
        // Get the audio file path
        rawAudioName = getFilePath("temp_eyes_rec.raw");
        newAudioName = getFilePath(AUDIO_WAV_FILENAME_DEFAULT);
        // Get buffer size
        bufferSizeInBytes = AudioRecord.getMinBufferSize(AudioRecordSetting.AUDIO_SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        // Create the audio record instance
        audioRecord = new AudioRecord(AudioRecordSetting.AUDIO_INPUT, AudioRecordSetting.AUDIO_SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes);
    }


    /**
     * A simple encapsulation of threads responsible for writing data.
     */
    private class AudioRecordThread implements Runnable {
        @Override
        public void run() {
            writeDataToFile(); // Write raw data
            copyWaveFile(rawAudioName, newAudioName); // Add header
        }
    }


    /**
     * Write raw audio data to file.
     * The raw data must be converted manually to other available high-level audio files.
     */
    private void writeDataToFile() {
        byte[] audioData = new byte[bufferSizeInBytes];
        File file = new File(rawAudioName);
        if (file.exists()) {
            file.delete();
        }
        int readSize = 0;
        try (FileOutputStream fos = new FileOutputStream(file)) {
            while (isRecord) {
                readSize = audioRecord.read(audioData, 0, bufferSizeInBytes);
                if (AudioRecord.ERROR_INVALID_OPERATION != readSize) {
                    try {
                        fos.write(audioData);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyWaveFile(String inFilename, String outFilename) {
        FileInputStream in;
        FileOutputStream out;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = AudioRecordSetting.AUDIO_SAMPLE_RATE;
        int channels = 2;
        long byteRate = 16 * AudioRecordSetting.AUDIO_SAMPLE_RATE * channels / 8;
        byte[] data = new byte[bufferSizeInBytes];
        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;
            writeWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);
            while (in.read(data) != -1) {
                out.write(data);
            }
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write the header info of the wav file.
     */
    private void writeWaveFileHeader(FileOutputStream out, long totalAudioLen,
                                     long totalDataLen, long longSampleRate, int channels, long byteRate)
            throws IOException {
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8); // block align
        header[33] = 0;
        header[34] = 16; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
    }

    /**
     * Get the path of the encoded audio file.
     */
    public static String getFilePath(String filename) {
        String mAudioWavPath = "";
        if (existsSDCard()) {
            String fileBasePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            mAudioWavPath = fileBasePath + "/" + filename;
        }
        return mAudioWavPath;
    }

    static class AudioRecordSetting {
        private final static int AUDIO_INPUT = MediaRecorder.AudioSource.MIC;
        private final static int AUDIO_SAMPLE_RATE = 44100;  // Sample rate is 44.1 kHz
    }

    private final static class AudioErrorCode {

        final static int SUCCESS = 1000;
        final static int E_NO_SDCARD = 1001;
        final static int E_STATE_RECODING = 1002;
        final static int E_UNKNOWN = 1003;


        public static String getErrorInfo(int type) {
            switch (type) {
                case SUCCESS:
                    return "success";
                case E_NO_SDCARD:
                    return "No SD Card";
                case E_STATE_RECODING:
                    return "Error State Recoding";
                case E_UNKNOWN:
                default:
                    return "Error Unknown";
            }
        }
    }
}
