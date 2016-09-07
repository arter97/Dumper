package com.arter97.dumper;

import android.content.Intent;
import android.content.res.AssetManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn = (Button)findViewById(R.id.button);
        btn.setOnClickListener(this);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                startDump();
                break;
        }
    }

    private void startDump() {
        TextView tv1 = (TextView)findViewById(R.id.TextView1);
        TextView tv2 = (TextView)findViewById(R.id.TextView2);

        // Copy from assets
        copyAssets(getApplicationInfo().dataDir + "/files");

        // Run dump.sh
        Runtime rt = Runtime.getRuntime();
        String[] commands = {getFilesDir().getAbsolutePath() + "/files/dump.sh",
                getFilesDir().getAbsolutePath() + "/files", // arg1 : self-path
        };

        Process proc;

        try {
            proc = rt.exec(commands);
        } catch (Exception e) {
            tv1.setText("ERROR!");
            tv2.setText("Please try again");
            return;
        }

        // Exec'ed, set text
        Button btn = (Button)findViewById(R.id.button);

        BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(proc.getInputStream()));

        // read the output from the command
        String s;
        try {
            while ((s = stdInput.readLine()) != null) {
                if (s.contains("STAGE"))
                    tv1.setText(s);
                else
                    tv2.setText(s);
            }
        } catch (Exception e) {
            // Do nothing
        }

        // Wait until process ends
        while (isRunning(proc)) {
            sleep(100);
        }

        tv1.setText("");
        tv2.setText("");
        btn.setText("Done!");
    }

    public boolean isRunning(Process process) {
        try {
            process.exitValue();
            return false;
        } catch (Exception e) {
            return true;
        }
    }
    public void sleep(long mili) {
        try {
            Thread.sleep(mili);
        } catch (Exception e) {
            // Do nothing
        }
    }

    private void copyAssets(String path) {
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            // Do nothing
        }
        path += '/';
        File destDir = new File(path);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        if (files != null) for (String filename : files) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(filename);
                File outFile = new File(path, filename);
                out = new FileOutputStream(outFile);
                copyFile(in, out);
            } catch (IOException e) {
                // Do nothing
            }
            finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
            }
        }
    }
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }
}
