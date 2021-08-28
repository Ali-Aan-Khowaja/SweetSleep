package com.softaan.sweetsleep;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.chaquo.python.Python;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.Arrays;

public class ShowResults extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_results);
        final Toolbar toolbar = findViewById(R.id.toolbar3);
        final ImageButton backButton = findViewById(R.id.backButton);
        final TextView sleepTime = findViewById(R.id.totalSleepTime);
        final TextView dsTime = findViewById(R.id.deepSleepTime);
        final TextView lsTime = findViewById(R.id.lightSleepTime);
        final TextView remTime = findViewById(R.id.REMSleepTime);
        final TextView wakeTime = findViewById(R.id.totalWakeTime);
        final PieChart pieChart = findViewById(R.id.pieChart);
        String fileName = getIntent().getStringExtra("File Name");

        Python.getInstance().getModule("script").callAttr("main", getFilesDir().getPath() + "/" + fileName);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShowResults.this, MainActivity.class);
                startActivity(intent);
            }
        });

        toolbar.setTitle(fileName);

        StringBuffer stringBuffer = new StringBuffer();
        try {
            //Attaching BufferedReader to the FileInputStream by the help of InputStreamReader
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(
                    openFileInput(fileName + ".json")));
            String inputString;
            //Reading data line by line and storing it into the stringbuffer
            while ((inputString = inputReader.readLine()) != null) {
                stringBuffer.append(inputString + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String dataSummary = stringBuffer.toString();
        dataSummary = dataSummary.replace("{", "");
        dataSummary = dataSummary.replace("}", "");

        float deepSleepTime =  Float.parseFloat(dataSummary.split(",")[0].split(":")[1]);
        float lightSleepTime = Float.parseFloat(dataSummary.split(",")[1].split(":")[1]);
        float remSleepTime = Float.parseFloat(dataSummary.split(",")[2].split(":")[1]);
        float totalWakeTime = Float.parseFloat(dataSummary.split(",")[3].split(":")[1]);
        float totalSleepTime = deepSleepTime + lightSleepTime + remSleepTime;

        sleepTime.setText(minutesToHours(totalSleepTime));
        dsTime.setText(minutesToHours(deepSleepTime));
        lsTime.setText(minutesToHours(lightSleepTime));
        remTime.setText(minutesToHours(remSleepTime));
        wakeTime.setText(minutesToHours(totalWakeTime));

        float deepSleepPercent = (deepSleepTime/totalSleepTime) * 100;
        float lightSleepPercent = (lightSleepTime/totalSleepTime) * 100;
        float remSleepPercent = (remSleepTime/totalSleepTime) * 100;
        
        pieChart.addPieSlice(new PieModel("Deep Sleep", deepSleepPercent, Color.parseColor("#FFA726")));
        pieChart.addPieSlice(new PieModel("Light Sleep", lightSleepPercent, Color.parseColor("#EF5350")));
        pieChart.addPieSlice(new PieModel("REM", remSleepPercent, Color.parseColor("#29B6F6")));

        // To animate the pie chart
        pieChart.startAnimation();
    }
    String minutesToHours(float totalMinutes){
        totalMinutes /= 60;
        DecimalFormat format = new DecimalFormat(".00");
        totalMinutes = Float.parseFloat(format.format(totalMinutes));
        String minutes, hours;
        if (Float.toString(totalMinutes).contains(".")) {
            hours = Float.toString(totalMinutes).split("\\.")[0];
            minutes = Integer.toString((int) Math.round(Float.parseFloat("0." + Float.toString(totalMinutes).split("\\.")[1]) * 60));
            return hours + " Hrs " + minutes + " Min";
        } else {
            return Float.toString(totalMinutes);
        }
    }
}