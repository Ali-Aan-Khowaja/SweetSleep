package com.softaan.sweetsleep;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TimePicker;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

    private String deviceName = null;
    private String deviceAddress;
    public String data;
    public RecyclerView files;
    public RecyclerView.Adapter storedFileAdapter;
    public static Handler handler;
    public static BluetoothSocket mmSocket;
    public static ConnectedThread connectedThread;
    public static CreateConnectThread createConnectThread;

    private final static int CONNECTING_STATUS = 1; // used in bluetooth handler to identify message status
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI Initialization
//        final ImageButton buttonConnect = findViewById(R.id.buttonConnect);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        final ProgressBar progressBar = findViewById(R.id.progressBar);
        final FloatingActionButton sync = findViewById(R.id.sync);
        toolbar.getMenu().findItem(R.id.disconnect).setVisible(false);
        toolbar.getMenu().findItem(R.id.disconnect).setEnabled(false);
        toolbar.getMenu().findItem(R.id.setAlarm).setVisible(false);
        toolbar.getMenu().findItem(R.id.setAlarm).setEnabled(false);

        progressBar.setVisibility(View.GONE);
        sync.setEnabled(false);

        data = "Ax,Ay,Az,Mx,My,Mz,Gx,Gy,Gz\n" +
                "6.28,0.27,7.26,-442,1451,-92,19,11,-1\n" +
                "6.28,1.88,7.06,-391,1288,-343,-12,9,-3\n" +
                "6,1.84,7.14,-375,1327,-331,30,17,4\n" +
                "6.08,0.47,7.3,-436,1433,-183,36,6,-8\n" +
                "4.98,0.75,7.92,-198,1477,-333,27,8,-1\n" +
                "4.94,0.78,7.92,-191,1482,-328,10,14,-1\n" +
                "5.73,0.35,7.57,-202,1495,-310,15,12,2\n" +
                "5.77,0.39,7.53,-202,1507,-297,7,8,-4\n" +
                "5.73,0.35,7.49,-181,1497,-321,26,13,-2\n" +
                "5.77,0.27,7.49,-193,1517,-305,7,15,-2\n" +
                "5.77,0.27,7.57,-186,1505,-315,12,7,-2\n" +
                "5.77,0.24,7.57,-186,1497,-307,21,-2,2\n" +
                "5.69,0.35,7.53,-181,1507,-295,4,4,0\n" +
                "5.73,0.27,7.65,-175,1500,-312,12,12,0\n" +
                "5.73,0.24,7.49,-172,1505,-300,16,11,0\n" +
                "5.65,0.24,7.57,-186,1495,-302,15,6,-2\n" +
                "5.77,0.24,7.57,-172,1507,-302,20,12,2\n" +
                "5.81,0.59,7.53,-170,1475,-333,9,10,-3\n" +
                "5.73,0.71,7.53,-165,1480,-323,14,13,-2\n" +
                "5.33,0.08,7.61,-130,1505,-315,23,12,0\n" +
                "5.37,0,7.73,-122,1500,-312,19,11,0\n" +
                "5.41,-0.04,7.65,-118,1527,-290,22,13,5\n" +
                "5.41,0,7.69,-103,1517,-310,13,15,4\n" +
                "5.37,0.04,7.73,-111,1530,-292,13,10,4\n" +
                "5.3,0,7.69,-118,1517,-297,17,16,7\n" +
                "5.33,-0.04,7.69,-118,1517,-292,19,9,-1\n" +
                "5.37,0,7.69,-127,1512,-292,14,6,1\n" +
                "5.37,0,7.77,-125,1522,-295,20,10,1\n" +
                "5.41,-0.04,7.81,-108,1517,-290,20,11,1\n" +
                "5.33,-0.08,7.69,-122,1512,-297,29,8,-1\n" +
                "5.37,-0.08,7.73,-122,1527,-292,19,9,4\n" +
                "5.33,-0.12,7.77,-116,1515,-297,14,11,-1\n" +
                "5.33,-0.08,7.69,-118,1532,-277,19,8,3\n" +
                "5.37,-0.08,7.73,-116,1525,-302,18,12,2\n" +
                "5.33,-0.04,7.73,-118,1517,-267,16,11,-2\n" +
                "5.45,-0.43,7.65,-146,1532,-233,13,10,1\n" +
                "6.08,0.55,7.34,-500,1470,-173,18,9,3\n" +
                "6.08,0.59,7.34,-482,1482,-150,10,5,3\n" +
                "6.08,0.59,7.37,-490,1465,-158,14,7,1\n" +
                "6,0.55,7.3,-463,1462,-142,46,3,1\n" +
                "6,0.59,7.37,-460,1490,-145,19,14,1\n" +
                "6,0.59,7.37,-485,1475,-161,16,10,2\n" +
                "6,0.59,7.3,-466,1472,-155,20,11,1\n" +
                "5.96,0.63,7.41,-476,1470,-152,10,13,5\n" +
                "5.57,0.16,7.61,-495,1448,-100,11,4,-1\n" +
                "5.88,0.59,7.41,-356,1443,-171,1,13,0\n" +
                "5.96,0.59,7.41,-351,1441,-161,39,6,0\n" +
                "5.88,0.39,7.45,-346,1443,-140,15,7,2\n" +
                "5.96,0.43,7.45,-343,1446,-147,17,7,-1\n" +
                "5.92,0.43,7.45,-356,1446,-135,18,11,-2\n" +
                "5.88,0.43,7.41,-335,1448,-157,22,7,0\n" +
                "5.92,0.47,7.37,-341,1438,-140,12,12,1\n" +
                "5.88,0.31,7.45,-356,1446,-137,2,10,-1\n" +
                "5.84,0.43,7.53,-351,1441,-135,15,9,2\n" +
                "5.92,0.35,7.41,-358,1446,-158,23,9,0\n" +
                "5.92,0.39,7.41,-351,1453,-142,16,10,-2\n" +
                "5.88,0.31,7.45,-341,1462,-137,8,12,3\n" +
                "5.88,0.35,7.45,-348,1448,-145,11,8,1\n" +
                "5.84,0.31,7.41,-343,1453,-150,12,8,-1\n" +
                "5.92,0.27,7.45,-335,1458,-150,11,9,0\n" +
                "5.84,0.24,7.37,-351,1446,-140,16,8,-3\n" +
                "5.81,0.35,7.45,-358,1448,-135,1,13,-2\n" +
                "5.88,0.31,7.45,-337,1462,-137,12,7,0\n" +
                "5.88,0.35,7.45,-348,1451,-140,16,11,0\n" +
                "5.84,0.31,7.45,-346,1456,-142,13,6,1\n" +
                "5.81,0.2,7.45,-335,1453,-140,23,4,1\n" +
                "5.81,0.27,7.37,-358,1467,-142,9,10,1\n" +
                "5.84,0.31,7.49,-348,1453,-150,11,13,0\n" +
                "5.81,0.31,7.49,-351,1443,-145,12,11,-4\n" +
                "5.84,0.31,7.49,-341,1453,-147,23,5,0\n" +
                "5.88,0.31,7.49,-348,1448,-137,32,8,-3\n" +
                "5.81,0.31,7.45,-343,1451,-152,20,9,-1\n" +
                "5.84,0.31,7.45,-351,1453,-145,10,7,-3\n" +
                "5.81,0.27,7.45,-343,1446,-145,13,12,-5\n" +
                "5.84,0.35,7.49,-332,1446,-150,13,7,-2\n" +
                "5.73,0.47,7.49,-276,1448,-173,9,8,-2\n" +
                "4.94,-2.2,7.69,-61,1700,0,12,15,-2\n" +
                "3.02,1.22,8.75,325,1418,-373,16,15,-4\n" +
                "2.98,0.98,8.67,343,1456,-353,2,22,-15\n" +
                "4.79,1.22,8,412,1231,-641,19,10,-4\n" +
                "4.79,1.33,7.96,415,1226,-666,12,10,-2\n" +
                "4.82,1.26,7.96,412,1231,-668,14,11,0\n" +
                "4.75,1.29,7.96,412,1228,-653,12,14,-3\n" +
                "4.75,1.26,7.92,412,1238,-648,23,6,-8\n" +
                "4.86,1.29,8,410,1221,-658,22,9,5\n" +
                "4.75,1.33,7.92,420,1233,-656,11,11,-2\n" +
                "4.82,1.26,7.92,407,1233,-663,10,11,4\n" +
                "4.79,1.29,7.96,412,1233,-656,26,11,-5\n" +
                "4.75,1.29,7.96,428,1233,-678,10,7,4\n" +
                "3.77,7.61,4.55,-1151,365,53,14,5,-3\n" +
                "3.69,7.57,4.51,-1121,390,21,9,7,1\n" +
                "3.73,7.65,4.55,-1106,375,21,12,6,-2\n" +
                "3.65,7.69,4.59,-1113,360,33,13,7,-2\n" +
                "3.69,7.61,4.51,-1111,365,46,12,9,-2\n" +
                "3.69,7.81,4.55,-1116,375,36,19,13,-3\n" +
                "3.57,7.85,4.67,-1118,357,13,16,7,-1\n" +
                "3.57,7.69,4.63,-1111,362,23,11,13,1\n" +
                "3.57,7.81,4.55,-1108,357,16,9,21,-1\n" +
                "3.53,8,4.35,-1085,335,21,8,10,-3\n" +
                "3.49,7.81,4.35,-1092,332,21,13,2,-2\n" +
                "3.45,7.81,4.35,-1087,325,18,10,13,1\n" +
                "3.45,7.88,4.43,-1087,330,3,23,7,-2\n" +
                "3.49,7.81,4.39,-1078,327,13,16,4,-5\n" +
                "3.26,7.3,4.9,-1108,360,1,13,9,0\n" +
                "3.3,7.37,4.86,-1092,375,6,24,0,1\n" +
                "3.26,7.49,4.71,-1071,340,11,12,18,-4\n" +
                "3.26,7.37,4.75,-1085,345,6,22,13,-7\n" +
                "2.98,7.73,4.71,-1036,286,-3,6,20,-1\n" +
                "2.94,7.65,4.55,-1028,286,-21,11,13,-9\n" +
                "3.06,7.57,5.02,-1116,342,-1,9,10,5\n" +
                "3.33,8.79,-0.71,-1245,113,277,2246,264,16\n" +
                "3.26,7.57,4.82,-1111,352,-3,16,12,1\n" +
                "3.3,7.49,4.82,-1118,335,-8,18,7,-1\n" +
                "3.18,8.12,3.77,-1005,235,26,8,7,-2\n" +
                "3.22,8.04,4,-1036,251,11,8,15,0\n" +
                "3.22,8.04,3.96,-1023,253,6,4,5,-2\n" +
                "3.14,8,3.96,-1028,261,11,-13,18,-6\n" +
                "3.3,8.04,4,-1038,243,13,10,10,-11\n" +
                "3.3,8.04,3.96,-1028,240,1,14,4,-1\n" +
                "3.3,8.04,3.92,-1047,253,26,21,4,1\n" +
                "3.3,7.88,4.08,-1033,248,1,10,12,-5\n" +
                "3.1,9.06,-3.02,-801,-128,297,-136,5,-14\n" +
                "3.45,8.63,2.47,-1250,286,-110,15,18,-8\n" +
                "3.53,9.06,0.04,-1268,170,8,0,14,-3\n" +
                "3.65,9.06,0.39,-1241,190,0,6,10,-7\n" +
                "3.57,9.18,0.39,-1257,195,-3,21,14,-8\n" +
                "3.61,8.67,2.63,-1023,273,-125,18,16,-2\n" +
                "5.33,1.18,7.61,-311,1372,-181,-3,10,-10\n" +
                "5.41,0.94,7.61,-325,1413,-155,24,17,-12\n" +
                "5.45,0.82,7.53,-311,1408,-166,10,10,-3\n" +
                "5.53,0.86,7.65,-332,1423,-157,19,14,-6\n" +
                "-8,5.49,3.92,250,-641,-478,129,147,-51\n" +
                "-4.9,-0.67,7.85,-61,263,-602,15,9,-9\n" +
                "-4.71,-0.94,7.96,-66,271,-625,23,8,0\n" +
                "-4.59,-1.14,7.92,-61,278,-602,12,10,-10\n" +
                "-4.67,-1.18,7.88,-71,278,-592,20,9,-5\n" +
                "-4.28,-1.84,8.04,-66,400,-605,9,15,-10\n" +
                "-4.47,-2.04,7.77,2,406,-600,16,6,0\n" +
                "-2.9,-2.86,7.96,-306,717,-402,64,18,-10\n" +
                "-3.14,-2.55,8.28,-207,623,-467,-27,10,-21\n" +
                "-2.79,-2.82,8.2,-380,638,-427,5,10,-11\n" +
                "0,-5.26,8.08,-708,382,-346,212,-400,-335\n" +
                "4.75,1.33,7.85,-148,-655,-125,28,10,-7\n" +
                "4.82,1.45,7.85,-122,-715,-112,17,9,-7\n" +
                "0.12,-0.08,9.18,-410,-350,-277,16,12,-5\n" +
                "0.47,-4.82,8.08,-463,180,-432,37,-13,-7\n" +
                "0.35,-4.79,8,-476,182,-427,18,14,-5\n" +
                "0.31,-4.79,7.88,-478,185,-432,14,9,-4\n" +
                "0.35,-4.75,7.96,-473,205,-432,16,10,-4\n" +
                "1.1,4.94,7.85,383,-840,132,26,10,-3\n" +
                "0.47,5.14,7.61,503,-908,173,20,14,-1\n" +
                "2.12,3.96,8.04,26,-898,8,20,10,-8\n" +
                "2.12,5.22,7.45,137,-931,188,14,16,-9\n" +
                "1.96,5.1,7.49,151,-921,168,15,10,-8\n" +
                "1.84,5.3,7.49,165,-926,168,13,11,-9\n" +
                "1.92,5.37,7.49,165,-921,183,13,9,-7\n" +
                "1.84,5.33,7.3,193,-923,198,16,15,-8\n" +
                "1.88,5.41,7.34,167,-933,193,9,14,-8\n" +
                "1.8,5.41,7.3,172,-921,208,17,13,-6\n" +
                "1.77,5.37,7.41,186,-921,208,6,13,-9\n" +
                "1.77,5.41,7.41,205,-933,198,14,15,-6\n" +
                "1.65,5.45,7.22,223,-923,242,16,12,-8\n" +
                "1.77,5.65,7.3,226,-931,242,15,15,-5\n" +
                "1.69,5.65,7.3,247,-923,240,16,15,-6\n" +
                "1.65,5.61,7.26,247,-933,242,12,13,-7\n" +
                "1.61,6.83,6.32,343,-918,430,15,10,-12\n" +
                "1.45,6.86,6.16,353,-918,455,19,11,-10\n" +
                "1.53,6.86,6.24,353,-926,452,17,10,-11\n" +
                "1.49,6.86,6.12,346,-911,462,14,12,-9\n" +
                "1.53,6.9,6.12,348,-911,455,14,11,-7\n" +
                "1.49,6.94,6.2,358,-921,472,9,13,-12\n" +
                "1.45,6.86,6.16,351,-916,457,14,13,-11\n" +
                "1.49,6.94,6.2,353,-913,472,13,13,-8\n" +
                "1.49,6.86,6.16,365,-921,440,19,15,-3\n" +
                "1.49,6.94,6.24,353,-908,457,9,11,-11\n" +
                "1.41,6.98,6.28,372,-913,467,14,14,-6\n" +
                "1.45,7.06,6.24,358,-901,472,17,15,-9\n" +
                "1.45,6.98,6.12,358,-916,460,14,12,-9\n" +
                "1.45,6.94,6.24,370,-911,462,8,14,-11\n" +
                "1.33,6.9,6.04,348,-913,486,12,10,-8\n" +
                "1.41,6.94,6.16,370,-918,472,14,13,-6\n" +
                "1.33,6.98,6.2,356,-911,478,17,13,-8\n" +
                "1.41,6.94,6.16,362,-913,467,12,10,-11\n" +
                "1.29,6.9,6.12,372,-913,467,13,15,-9\n" +
                "1.49,6.98,6.24,365,-916,467,11,13,-12\n" +
                "1.33,7.06,6.32,365,-911,467,13,15,-8\n" +
                "1.37,7.06,6.2,362,-903,465,15,12,-8\n" +
                "1.37,6.98,6.08,353,-923,462,12,14,-10\n" +
                "1.29,7.02,6.08,372,-911,467,11,11,-11\n" +
                "1.29,6.94,6.12,365,-911,476,16,15,-13\n" +
                "1.29,6.98,6.12,358,-921,470,13,13,-14\n" +
                "1.26,6.98,6.12,375,-903,460,11,18,-8\n" +
                "1.29,7.06,6.08,377,-893,473,14,14,-14\n" +
                "1.26,7.14,6.08,377,-908,501,18,12,-10\n" +
                "1.29,7.18,6,367,-906,508,14,15,-8\n" +
                "1.22,7.18,5.92,362,-908,501,14,14,-14\n" +
                "1.22,7.18,5.92,377,-913,496,12,13,-11\n" +
                "1.26,7.14,5.96,383,-908,483,11,14,-11\n" +
                "1.26,7.14,5.96,375,-908,503,15,13,-12\n" +
                "1.29,7.1,5.92,372,-911,493,15,15,-13\n" +
                "1.29,7.18,6,386,-898,498,17,15,-12\n" +
                "1.29,7.14,6.04,370,-903,488,12,13,-12\n" +
                "1.26,7.18,5.92,375,-901,503,14,17,-13\n" +
                "1.26,7.14,6,380,-903,511,11,17,-10\n" +
                "1.33,7.18,6.08,380,-906,483,13,14,-15\n" +
                "1.73,6.24,6.79,242,-953,343,13,16,-10\n" +
                "-7.77,0.39,5.37,375,1300,-508,23,15,-9\n" +
                "-7.69,0.67,5.37,367,1288,-518,11,16,-12\n" +
                "-7.61,0.67,5.45,346,1286,-526,16,14,-7\n" +
                "-7.69,0.67,5.41,341,1296,-541,8,21,-6\n" +
                "-7.69,0.71,5.37,330,1273,-531,16,11,-8\n" +
                "-2.51,-6.63,6.2,-558,966,-437,4,5,-10\n" +
                "-2.47,-6.67,6.2,-580,951,-427,11,18,-4\n" +
                "-2.43,-6.71,6.16,-588,956,-432,12,6,-11\n" +
                "-2.2,-6.71,6.24,-607,941,-432,12,18,-6\n" +
                "-1.49,-7.49,5.61,-716,1047,-321,17,0,-13\n" +
                "-1.57,-7.61,5.45,-737,1030,-331,15,18,-7\n" +
                "0.24,-6.12,7.14,-897,690,-422,28,11,-8\n" +
                "0.31,-6.24,7.1,-916,672,-410,16,10,-9\n" +
                "0.31,-6.2,7.02,-908,675,-415,25,2,-13\n" +
                "0.51,-6.9,6.47,-916,741,-373,10,14,-6\n" +
                "-6.16,0.75,6.98,12,675,-762,20,15,-6\n" +
                "-6.2,0.78,6.98,-5,685,-747,13,16,-10\n" +
                "-0.24,4.98,7.88,866,-748,137,17,10,-8\n" +
                "-0.27,4.9,7.88,876,-746,132,14,11,-11\n" +
                "-0.35,5.02,7.77,908,-748,150,10,13,-11\n" +
                "-0.39,5.88,7.26,925,-761,262,13,12,-12\n" +
                "1.49,4.47,7.96,318,-931,6,16,12,-12\n" +
                "1.33,6.12,6.98,438,-958,267,24,12,-9\n" +
                "1.41,6.28,6.75,452,-941,310,15,16,-12\n" +
                "1.41,6.32,6.75,450,-961,305,17,14,-10\n" +
                "1.84,4.55,7.88,391,-906,-8,16,14,-11\n" +
                "1.84,4.55,7.85,405,-903,-1,13,13,-14\n" +
                "1.73,4.71,7.85,420,-916,18,10,14,-13\n" +
                "1.73,4.63,7.65,417,-921,11,19,11,-11\n" +
                "1.73,4.71,7.85,417,-908,16,18,15,-14\n" +
                "0.08,7.73,5.41,852,-727,531,20,15,-12\n" +
                "-1.45,5.06,7.61,1365,-42,-3,16,20,-13\n" +
                "0.04,-1.73,9.02,-620,478,-300,-51,295,273\n" +
                "0.71,-0.43,9.18,-786,286,-250,12,11,-13\n" +
                "0.75,-0.63,9.06,-751,283,-228,13,15,-11\n" +
                "0.82,-0.67,9.14,-761,306,-226,12,16,-12\n" +
                "0.75,-0.71,9.1,-753,303,-226,10,15,-14\n" +
                "0.9,-0.9,8.98,-746,276,-221,15,16,-12\n" +
                "0.94,-1.53,8.94,-743,377,-181,12,13,-15\n" +
                "0.9,-1.49,8.98,-740,367,-176,11,13,-14\n" +
                "0.86,-1.69,8.98,-743,352,-206,12,14,-15\n" +
                "0.82,-1.8,8.9,-713,355,-191,14,18,-15\n" +
                "1.02,-2.28,8.9,-743,248,-193,18,14,-13\n" +
                "1.02,-2.39,8.83,-732,253,-191,31,19,-13\n" +
                "-4.51,-3.02,7.53,188,170,-735,4,17,-16\n" +
                "-4.35,-3.65,7.34,170,232,-722,13,14,-10\n" +
                "-4.24,-3.73,7.45,156,217,-735,17,12,-4\n" +
                "-4.24,-3.57,6.75,47,200,-735,365,40,63\n" +
                "4.12,3.53,7.61,-285,-798,66,16,15,-9\n" +
                "4.12,3.65,7.49,-287,-812,120,12,12,-7\n" +
                "4.12,3.53,7.57,-301,-801,76,25,15,-11\n" +
                "4.16,3.57,7.57,-311,-793,87,14,13,-8\n" +
                "3.22,-0.35,8.79,-463,-487,-457,17,31,-8\n" +
                "3.84,1.18,8.28,-466,-623,-196,17,11,-11\n" +
                "3.53,-3.37,7.88,-562,-235,-612,17,12,-10\n" +
                "2.35,5.45,7.26,1271,-253,-305,12,11,-3\n" +
                "2.31,5.41,7.37,1271,-246,-265,18,11,-5\n" +
                "2.24,5.33,7.34,1281,-248,-285,14,17,-6\n" +
                "2.47,4.71,7.57,1262,-392,-312,17,14,-3\n" +
                "2.43,4.67,7.61,1262,-380,-307,15,10,-7\n" +
                "2.43,4.71,7.65,1247,-370,-321,16,9,-7\n" +
                "2.47,4.71,7.69,1266,-367,-315,13,7,-6\n" +
                "2.47,4.71,7.77,1287,-375,-333,15,13,-5\n" +
                "2.43,4.67,7.65,1271,-370,-338,9,15,-6\n" +
                "2.35,4.59,7.65,1290,-367,-305,14,13,-4\n" +
                "2.39,4.63,7.69,1273,-365,-316,14,13,-6\n" +
                "2.39,4.55,7.85,1255,-335,-315,13,12,-3\n" +
                "2.35,4.51,7.77,1281,-352,-333,11,18,-6\n" +
                "2.35,4.31,7.92,1266,-306,-307,16,8,-3\n" +
                "2.31,4.24,7.88,1273,-313,-321,16,17,-6\n" +
                "2.35,4.31,7.92,1271,-318,-316,13,15,-7\n" +
                "2.31,4.24,7.88,1285,-308,-318,13,16,-2\n" +
                "3.53,8.71,-3.88,-346,-350,48,-431,258,-17\n" +
                "4.67,4.59,6.94,900,495,-806,16,15,-11\n" +
                "4.59,4.51,6.98,921,505,-798,14,11,-3\n" +
                "4.43,4.51,6.98,940,497,-782,17,12,1\n" +
                "4.28,4.43,7.1,965,522,-793,14,15,-6\n" +
                "4.24,4.43,7.1,975,530,-788,15,10,-2\n" +
                "4.24,4.47,7.1,998,537,-772,16,17,-11\n" +
                "4.24,4.39,7.14,1001,530,-782,18,5,-8\n" +
                "4.2,4.47,7.18,1023,515,-780,17,15,-5\n" +
                "4.08,4.39,7.14,1028,530,-777,15,16,-7\n" +
                "4.16,4.47,7.26,1031,527,-777,19,17,-11\n" +
                "3.88,4.28,7.41,1071,565,-767,18,11,-7\n" +
                "5.57,4.24,6.32,718,421,-1023,11,10,-8\n" +
                "3.73,-6.71,5.65,-431,192,-750,23,9,-14\n" +
                "3.92,-6.59,5.69,-436,162,-762,17,15,-12\n" +
                "3.92,-6.35,5.88,-428,138,-796,16,15,-14\n" +
                "3.8,-6.43,5.77,-417,138,-787,17,13,-13\n" +
                "3.88,-6.51,5.81,-420,148,-808,18,11,-17\n" +
                "3.92,-6.47,5.81,-431,148,-823,12,13,-13\n" +
                "0.98,-2.55,8.75,-610,286,-221,17,19,-10\n" +
                "-0.16,-4.12,8.32,-612,278,-331,14,15,-8\n" +
                "-0.31,-4.08,8.43,-612,286,-363,15,17,-8\n" +
                "-0.24,-3.96,8.39,-602,271,-368,9,12,-11\n" +
                "-0.35,-4.98,7.88,-215,680,-437,12,541,731\n" +
                "-4.47,-0.82,10.4,-238,562,-465,-2740,409,-1002\n" +
                "-4.71,-3.14,7.49,-127,655,-625,29,-20,11\n" +
                "-4.71,-3.14,7.41,-113,670,-620,16,14,-9\n" +
                "-4.75,-3.22,7.41,-103,667,-625,15,14,-4\n" +
                "-4.75,-3.14,7.41,-108,685,-638,18,14,-4\n" +
                "-4.71,-3.06,7.41,-111,680,-633,15,9,2\n" +
                "-4.71,-3.1,7.37,-106,685,-641,8,7,-7\n" +
                "-5.57,-2.39,7.14,-90,946,-638,14,14,-7\n" +
                "-5.57,-2.43,7.06,-97,948,-630,19,7,-6\n" +
                "-5.49,-2.35,7.14,-101,931,-658,18,17,-6\n" +
                "-5.57,-2.39,7.1,-103,926,-643,10,13,-11\n" +
                "-5.57,-2.35,7.06,-106,911,-646,-3,-1,-5\n" +
                "-5.37,-1.41,7.37,-137,908,-638,16,9,0\n" +
                "-5.57,1.29,7.41,-15,286,-551,15,10,-1\n" +
                "-5.49,1.49,7.49,-26,316,-531,17,11,-4\n" +
                "-5.49,1.53,7.3,-12,298,-526,11,4,-13\n" +
                "-4.55,7.57,3.77,410,153,-740,8,11,-3\n" +
                "-4.51,7.69,3.77,391,156,-757,25,3,1\n" +
                "-5.02,6.12,5.1,513,598,-755,38,3,-6\n" +
                "-4.94,6.16,5.14,500,608,-762,16,14,-1\n" +
                "1.14,-0.67,9.06,-442,-372,-178,12,11,-4\n" +
                "1.22,-0.71,9.02,-438,-347,-161,16,16,-4\n" +
                "2.55,6.24,6.71,1045,-768,117,1,29,-3\n" +
                "2.28,6.2,6.71,1081,-761,142,10,10,-9\n" +
                "2.12,6.2,6.83,1095,-778,157,15,15,-9\n" +
                "1.96,6.12,6.9,1130,-788,161,11,14,-9\n" +
                "1.92,6.2,6.94,1142,-783,178,12,15,-8\n" +
                "1.8,6.24,6.86,1177,-783,173,15,11,-8\n" +
                "1.73,6.16,6.9,1222,-788,181,15,16,-9\n" +
                "1.69,6.12,6.9,1231,-781,188,12,12,-10\n" +
                "1.8,6,6.98,1146,-743,191,14,12,-10\n" +
                "0.75,3.02,8.59,1468,-301,-120,12,15,-11\n" +
                "0.67,2.98,8.79,1462,-291,-115,13,14,-11";
        writeData("20-Aug-2021");

        showFiles();

        // If a bluetooth device has been selected from SelectDeviceActivity
        deviceName = getIntent().getStringExtra("deviceName");
        if (deviceName != null){
            // Get the device address to make BT Connection
            deviceAddress = getIntent().getStringExtra("deviceAddress");
            // Show progress and connection status
            toolbar.setSubtitle("Connecting to " + deviceName + "...");
            progressBar.setVisibility(View.VISIBLE);
            toolbar.getMenu().findItem(R.id.connect).setEnabled(false);

            /*
            This is the most important piece of code. When "deviceName" is found
            the code will call a new thread to create a bluetooth connection to the
            selected device (see the thread code below)
             */
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            createConnectThread = new CreateConnectThread(bluetoothAdapter,deviceAddress);
            createConnectThread.start();
        }

        /*
        Second most important piece of Code. GUI Handler
         */
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg){
                switch (msg.what){
                    case CONNECTING_STATUS:
                        switch(msg.arg1){
                            case 1:
                                toolbar.setSubtitle("Connected to " + deviceName);
                                progressBar.setVisibility(View.GONE);
                                toolbar.getMenu().findItem(R.id.connect).setVisible(false);
                                toolbar.getMenu().findItem(R.id.connect).setEnabled(false);
                                toolbar.getMenu().findItem(R.id.disconnect).setVisible(true);
                                toolbar.getMenu().findItem(R.id.disconnect).setEnabled(true);
                                toolbar.getMenu().findItem(R.id.setAlarm).setVisible(true);
                                toolbar.getMenu().findItem(R.id.setAlarm).setEnabled(true);
                                sync.setEnabled(true);
                                break;
                            case -1:
                                toolbar.setSubtitle("Failed to connect");
                                progressBar.setVisibility(View.GONE);
                                toolbar.getMenu().findItem(R.id.connect).setEnabled(true);
                                sync.setEnabled(false);
                                break;
                        }
                        break;

                    case MESSAGE_READ:
                        String arduinoMsg = msg.obj.toString(); // Read message from Arduino
                        data += arduinoMsg + "\n";
                        break;
                }
                if (data.contains("Data Complete"))
//                    writeData();
                    showFiles();

            }
        };

        // Select Bluetooth Device
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId() == R.id.connect) {
                    Intent intent = new Intent(MainActivity.this, SelectDeviceActivity.class);
                    startActivity(intent);
                } else if(item.getItemId()== R.id.disconnect) {
                    connectedThread.cancel();
                    toolbar.setSubtitle("Disconnected");
                    toolbar.getMenu().findItem(R.id.connect).setEnabled(true);
                    toolbar.getMenu().findItem(R.id.connect).setVisible(true);
                    toolbar.getMenu().findItem(R.id.disconnect).setEnabled(false);
                    toolbar.getMenu().findItem(R.id.disconnect).setVisible(false);
                    toolbar.getMenu().findItem(R.id.setAlarm).setEnabled(false);
                    toolbar.getMenu().findItem(R.id.setAlarm).setVisible(false);
                    sync.setEnabled(false);
                } else if(item.getItemId()== R.id.setAlarm) {
                    DialogFragment newFragment = new TimePickerFragment();
                    newFragment.show(getSupportFragmentManager(), "timePicker");
                }
                return false;
            }
        });

        sync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectedThread.write("Send Data");
            }
        });
    }

    void showFiles() {
        ArrayList<StoredFile> files = initFiles();
        this.files = findViewById(R.id.files);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        this.files.setLayoutManager(mLayoutManager);

        storedFileAdapter = new StoredFileAdapter(MainActivity.this, files);
        this.files.setAdapter(storedFileAdapter);
    }

    private ArrayList<StoredFile> initFiles(){
        ArrayList<StoredFile> list = new ArrayList<>();
        File folder = new File(getFilesDir().getPath());
        File[] filesInFolder = folder.listFiles();
        for (File file : filesInFolder) {
            if (!file.isDirectory() && !file.getName().contains(".json") ) {
                list.add(new StoredFile(file.getName()));
            }
        }
        return list;
    }

    public String getDate(){
        Date now = new Date();
        String day = now.toString().split(" ")[2];
        String month = now.toString().split(" ")[1];
        String year = now.toString().split(" ")[5];
        return day + "-" + month + "-" + year;
    }

    public void writeData(String date){
        data = data.replace("Data Complete", "");
        data = data.replace("Send Data", "");
        data = data.replace("�", "");
        String fileName = date + ".csv";
        FileOutputStream fos;
        try {
            fos = openFileOutput(fileName, Context.MODE_PRIVATE);
            //default mode is PRIVATE, can be APPEND etc.
            fos.write(data.getBytes());
            fos.close();
        } catch (FileNotFoundException e) {e.printStackTrace();}
          catch (IOException e) {e.printStackTrace();}
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user
            connectedThread.write(Integer.toString(hourOfDay) + ":" + Integer.toString(minute));
        }
    }

    /* ============================ Thread to Create Bluetooth Connection =================================== */
    public static class CreateConnectThread extends Thread {

        public CreateConnectThread(BluetoothAdapter bluetoothAdapter, String address) {
            /*
            Use a temporary object that is later assigned to mmSocket
            because mmSocket is final.
             */
            BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
            BluetoothSocket tmp = null;
            UUID uuid = bluetoothDevice.getUuids()[0].getUuid();

            try {
                /*
                Get a BluetoothSocket to connect with the given BluetoothDevice.
                Due to Android device varieties,the method below may not work fo different devices.
                You should try using other methods i.e. :
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                 */
                tmp = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);

            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            bluetoothAdapter.cancelDiscovery();
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
                Log.e("Status", "Device connected");
                handler.obtainMessage(CONNECTING_STATUS, 1, -1).sendToTarget();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                    Log.e("Status", "Cannot connect to device");
                    handler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            connectedThread = new ConnectedThread(mmSocket);
            connectedThread.run();
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    /* =============================== Thread for Data Transfer =========================================== */
    public static class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException ignored) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes = 0; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    /*
                    Read from the InputStream from Arduino until termination character is reached.
                    Then send the whole String message to GUI Handler.
                     */
                    buffer[bytes] = (byte) mmInStream.read();
                    String readMessage;
                    if (buffer[bytes] == '\n'){
                        readMessage = new String(buffer,0,bytes);
                        Log.e("Arduino Message",readMessage);
                        handler.obtainMessage(MESSAGE_READ,readMessage).sendToTarget();
                        bytes = 0;
                    } else {
                        bytes++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes(); //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e("Send Error","Unable to send message",e);
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException ignored) { }
        }
    }

    /* ============================ Terminate Connection at BackPress ====================== */
    @Override
    public void onBackPressed() {
        // Terminate Bluetooth Connection and close app
        if (createConnectThread != null) {
            createConnectThread.cancel();
        }
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }
}