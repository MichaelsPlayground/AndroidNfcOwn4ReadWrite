package de.androidcrypto.androidnfcown1;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    // https://www.demo2s.com/android/android-mifareclassic-tutorial-with-examples.html
    // example 1

    // this works with a background thread like AndroidNfcOwn1, taken from MifareClassicToolOwn

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFilter;
    private String[][] techListsArray;

    TextView textView;

    Button mctRoutines;
    Intent mctRoutinesIntent;

    String TAG = "NfcOwn1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.data);
        mctRoutines = findViewById(R.id.btnMctRoutines);
        mctRoutinesIntent = new Intent(MainActivity.this, MctRoutines.class);


        //Variabili per l'nfc
        this.nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        this.pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        this.intentFilter = new IntentFilter[]{new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)};
        this.techListsArray = new String[][]{new String[]{MifareClassic.class.getName()}};

        mctRoutines.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(mctRoutinesIntent);
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        //Leggo il tag
        super.onNewIntent(intent);
        Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        // https://stackoverflow.com/questions/17401154/android-nfc-java-io-ioexception-transceive-failed
        MifareClassic mif = MifareClassic.get(tag);

        int ttype = mif.getType();
        textView.setText("");
        Log.d(TAG, "MifareClassic tag type: " + ttype);
        textView.setText(textView.getText() + "\n" + "MifareClassic tag type: " + ttype);

        int tsize = mif.getSize();
        Log.d(TAG, "tag size: " + tsize);
        textView.setText(textView.getText() + "\n" + "tag size: " + tsize);

        int s_len = mif.getSectorCount();
        Log.d(TAG, "tag sector count: " + s_len);
        textView.setText(textView.getText() + "\n" + "tag sector count: " + s_len);

        int b_len = mif.getBlockCount();
        Log.d(TAG, "tag block count: " + b_len);
        textView.setText(textView.getText() + "\n" + "tag block count: " + b_len);

        Log.d(TAG, "## STARTING READING THREAD ##");
        textView.setText(textView.getText() + "\n" + "## STARTING READING THREAD ##");

        Thread thread = new Thread(){
            public void run(){
                System.out.println("Thread Running");
                Log.d(TAG, "## THREAD RUNNING ##");
                textView.setText(textView.getText() + "\n" + "## THREAD RUNNING ##");
                readTagOwn(mif);
            }
        };

        thread.start();



        /*
        MifareClassic tesseraMifare = MifareClassic.get(tag);
        try {

            tesseraMifare.connect();
            byte[] keyb = {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};


             // Chiave A utilizzata per le tessere Mifare

            byte[] keya = {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};

            tesseraMifare.authenticateSectorWithKeyA(0, keya);

            // ottengo un bytearray contenente i dati del blocco 2
            //byte[] codice = tesseraMifare.readBlock(2);
            // fehler

            // wir versuchen mal block 0
            byte[] codice = tesseraMifare.readBlock(0);

            tesseraMifare.close();

        } catch (IOException e) {
            e.printStackTrace();
        }*/

    }

    // you need to use this method to write to the textview from a background thread
    // source: https://stackoverflow.com/a/25488292/8166854
    private void setText(final TextView text,final String value){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText(value);
            }
        });
    }

    // this code reads all elements
    private void readTagOwn(MifareClassic mif) {
        String output = "";
        int s_len = mif.getSectorCount();
        try {
            mif.connect();
            if (mif.isConnected()) {

                for (int sectorCount = 0; sectorCount < s_len; sectorCount++) {

                    boolean isAuthenticated = false;
                    // try to authenticate with known keys - no brute force
                    if (mif.authenticateSectorWithKeyA(sectorCount, MifareClassic.KEY_MIFARE_APPLICATION_DIRECTORY)) {
                        isAuthenticated = true;
                    } else if (mif.authenticateSectorWithKeyA(sectorCount, MifareClassic.KEY_DEFAULT)) {
                        isAuthenticated = true;
                    } else if (mif.authenticateSectorWithKeyA(sectorCount, MifareClassic.KEY_NFC_FORUM)) {
                        isAuthenticated = true;
                    } else if (mif.authenticateSectorWithKeyB(sectorCount, MifareClassic.KEY_DEFAULT)) {
                        isAuthenticated = true;
                    } else if (mif.authenticateSectorWithKeyB(sectorCount, MifareClassic.KEY_MIFARE_APPLICATION_DIRECTORY)) {
                        isAuthenticated = true;
                    } else if (mif.authenticateSectorWithKeyB(sectorCount, MifareClassic.KEY_NFC_FORUM)) {
                        isAuthenticated = true;

                    } else {
                        Log.d("TAG", "sector: " + sectorCount + " Authorization denied ");
                        output = textView.getText() + "\n" + "sector: " + sectorCount + " Authorization denied ";
                        // gives an error, no access to textview from background thread
                        // textView.setText(textView.getText() + "\n" + "sector: " + sectorCount + " Authorization denied ");
                    }

                    if (isAuthenticated) {

                        // get the blockindex
                        int block_index = mif.sectorToBlock(sectorCount);
                        Log.d(TAG, "blockindex: " + block_index);
                        output = output + "\n" + "bl_index: " + block_index;
                        //textView.setText(textView.getText() + "\n" + "bl_index: " + block_index);

                        // get block in sector
                        int blocksInSector = mif.getBlockCountInSector(sectorCount);
                        Log.d(TAG, "blocksInSector: " + blocksInSector);
                        output = output + "\n" + "bl_in_sect: " + blocksInSector;
                        //textView.setText(textView.getText() + "\n" + "bl_in_sect: " + blocksInSector);

                        // get the data of each block
                        byte[] block = mif.readBlock(block_index);
                        String s_block = getHexString(block, block.length);
                        Log.d(TAG, "sector: " + sectorCount + " data: " + s_block);
                        output = output + "\n" + "sector: " + sectorCount + " dat:" + s_block;
                        //textView.setText(textView.getText() + "\n" + "sector: " + sectorCount + " dat:" + s_block);

                        // first block is already read
                        for (int blockInSectorCount = 1; blockInSectorCount < blocksInSector; blockInSectorCount++) {
                            // get following data
                            block = mif.readBlock((block_index + blockInSectorCount));
                            s_block = getHexString(block, block.length);
                            Log.d(TAG, "sector: " + sectorCount + " data: " + s_block);
                            output = output + "\n" + "blsnr: " + blocksInSector + " d:" + s_block;
                            //textView.setText(textView.getText() + "\n" + "blsnr: " + blocksInSector + " d:" + s_block);

                        }
                    }
                }
            }
            mif.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        setText(textView, output);
    }

    // the problem with this code is... it is running so fast that the ui can't update all textview fields
    private void readTagOwnOld(MifareClassic mif) {
        int s_len = mif.getSectorCount();
        try {
            mif.connect();
            if (mif.isConnected()) {

                for (int sectorCount = 0; sectorCount < s_len; sectorCount++) {

                    boolean isAuthenticated = false;
                    // try to authenticate with known keys - no brute force
                    if (mif.authenticateSectorWithKeyA(sectorCount, MifareClassic.KEY_MIFARE_APPLICATION_DIRECTORY)) {
                        isAuthenticated = true;
                    } else if (mif.authenticateSectorWithKeyA(sectorCount, MifareClassic.KEY_DEFAULT)) {
                        isAuthenticated = true;
                    } else if (mif.authenticateSectorWithKeyA(sectorCount, MifareClassic.KEY_NFC_FORUM)) {
                        isAuthenticated = true;
                    } else if (mif.authenticateSectorWithKeyB(sectorCount, MifareClassic.KEY_DEFAULT)) {
                        isAuthenticated = true;
                    } else if (mif.authenticateSectorWithKeyB(sectorCount, MifareClassic.KEY_MIFARE_APPLICATION_DIRECTORY)) {
                        isAuthenticated = true;
                    } else if (mif.authenticateSectorWithKeyB(sectorCount, MifareClassic.KEY_NFC_FORUM)) {
                        isAuthenticated = true;

                    } else {
                        Log.d("TAG", "sector: " + sectorCount + " Authorization denied ");
                        setText(textView, textView.getText() + "\n" + "sector: " + sectorCount + " Authorization denied ");
                        // gives an error, no access to textview from background thread
                        // textView.setText(textView.getText() + "\n" + "sector: " + sectorCount + " Authorization denied ");
                    }

                    if (isAuthenticated) {

                        // get the blockindex
                        int block_index = mif.sectorToBlock(sectorCount);
                        Log.d(TAG, "blockindex: " + block_index);
                        setText(textView, textView.getText() + "\n" + "bl_index: " + block_index);
                        //textView.setText(textView.getText() + "\n" + "bl_index: " + block_index);

                        // get block in sector
                        int blocksInSector = mif.getBlockCountInSector(sectorCount);
                        Log.d(TAG, "blocksInSector: " + blocksInSector);
                        setText(textView, textView.getText() + "\n" + "bl_in_sect: " + blocksInSector);
                        //textView.setText(textView.getText() + "\n" + "bl_in_sect: " + blocksInSector);

                        /*
                        // get the data of each block
                        for (int blockInSectorCount = 0; blockInSectorCount < blocksInSector; blockInSectorCount++) {
                            // get following data
                            byte[] block = mif.readBlock((block_index + blockInSectorCount));
                            String s_block = getHexString(block, block.length);
                            Log.d(TAG, "sector: " + sectorCount + " data: " + s_block);
                            textView.setText(textView.getText() + "\n" + "bliS: " + blockInSectorCount + " dat:" + s_block);
                        }*/

                        // get the data of each block
                        byte[] block = mif.readBlock(block_index);
                        String s_block = getHexString(block, block.length);
                        Log.d(TAG, "sector: " + sectorCount + " data: " + s_block);
                        setText(textView, textView.getText() + "\n" + "sector: " + sectorCount + " dat:" + s_block);
                        //textView.setText(textView.getText() + "\n" + "sector: " + sectorCount + " dat:" + s_block);

                        // first block is already read
                        for (int blockInSectorCount = 1; blockInSectorCount < blocksInSector; blockInSectorCount++) {
                            // get following data
                            block = mif.readBlock((block_index + blockInSectorCount));
                            s_block = getHexString(block, block.length);
                            Log.d(TAG, "sector: " + sectorCount + " data: " + s_block);
                            setText(textView, textView.getText() + "\n" + "blsnr: " + blocksInSector + " d:" + s_block);
                            //textView.setText(textView.getText() + "\n" + "blsnr: " + blocksInSector + " d:" + s_block);

                        }

                    }
                }
            }
            mif.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        this.nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilter, techListsArray);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.nfcAdapter.disableForegroundDispatch(this);
    }

    private String hexByte(byte b) {
        int pos = b;

        if (pos < 0) {
            pos += 256;
        }

        String returnString = new String();
        returnString += Integer.toHexString(pos / 16);
        returnString += Integer.toHexString(pos % 16);
        return returnString;
    }

    private static final byte[] HEX_CHAR_TABLE = {(byte) '0', (byte) '1', (byte) '2', (byte) '3', (byte) '4',
            (byte) '5', (byte) '6', (byte) '7', (byte) '8', (byte) '9', (byte) 'A', (byte) 'B', (byte) 'C',
            (byte) 'D', (byte) 'E', (byte) 'F'};

    public static String getHexString(byte[] raw, int len) {
        byte[] hex = new byte[2 * len];
        int index = 0;
        int pos = 0;

        for (byte b : raw) {
            if (pos >= len)
                break;

            pos++;
            int v = b & 0xFF;
            hex[index++] = HEX_CHAR_TABLE[v >>> 4];
            hex[index++] = HEX_CHAR_TABLE[v & 0xF];
        }

        return new String(hex);
    }

}