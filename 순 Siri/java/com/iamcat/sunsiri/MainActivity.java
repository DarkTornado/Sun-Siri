package com.iamcat.sunsiri;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    TextToSpeech tts;
    String[] chatData;
    String[] chatData2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            toast("음성 인식 기능을 위해, 해당 권한이 필요합니다.");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 5);
        }
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(1);
        final TextView chat = new TextView(this);
        chat.setText("\n");
        chat.setTextSize(18);
        chat.setTextColor(Color.BLACK);
        layout.addView(chat);
        Button input = new Button(this);
        input.setText("입력");
        input.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputVoice(chat);
            }
        });
        input.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                inputText(chat);
                return true;
            }
        });
        layout.addView(input);
        TextView maker = new TextView(this);
        maker.setText("\nCopyleft 2016-2024 고양이가 만들었다냥!\n");
        maker.setTextSize(13);
        maker.setTextColor(Color.BLACK);
        maker.setGravity(Gravity.CENTER);
        layout.addView(maker);
        int pad = dip2px(15);
        layout.setPadding(pad, pad, pad, pad);
        ScrollView scroll = new ScrollView(this);
        scroll.addView(layout);
        scroll.setBackgroundColor(Color.WHITE);
        setContentView(scroll);
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                tts.setLanguage(Locale.KOREAN);
            }
        });
        chatData = getChatData(false);
        chatData2 = getChatData(true);
    }

    private String[] getChatData(boolean tf){
        try{
            AssetManager am = this.getAssets();
            InputStreamReader isr;
            if(tf) isr = new InputStreamReader(am.open("chatData2.txt"));
            else isr = new InputStreamReader(am.open("chatData.txt"));
            BufferedReader br = new BufferedReader(isr);
            String str = br.readLine();
            String line = "";
            while ((line = br.readLine()) != null) {
                str += "\n" + line;
            }
            isr.close();
            br.close();
            return str.split("\n");
        }
        catch(Exception e){
            toast(e.toString());
        }
        return null;
    }

    public void inputVoice(final TextView chat){
        try{
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
            SpeechRecognizer stt = SpeechRecognizer.createSpeechRecognizer(this);
            stt.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    toast("입력 대기중...");
                }

                @Override
                public void onBeginningOfSpeech() {

                }

                @Override
                public void onRmsChanged(float rmsdB) {

                }

                @Override
                public void onBufferReceived(byte[] buffer) {

                }

                @Override
                public void onEndOfSpeech() {
                    toast("입력 완료");
                }

                @Override
                public void onError(int error) {
                    toast("오류 발생\n에러 코드 : " + error);
                }

                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> result = (ArrayList<String>) results.get(SpeechRecognizer.RESULTS_RECOGNITION);
                    final String que = result.get(0);
                    chat.setText(chat.getText() + "[나] " + que + "\n");
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            showAnswer(que, chat);
                        }
                    }, 2000);
                }

                @Override
                public void onPartialResults(Bundle partialResults) {

                }

                @Override
                public void onEvent(int eventType, Bundle params) {

                }
            });
            stt.startListening(intent);
        }
        catch(Exception e){
            toast(e.toString());
        }
    }

    private void inputText(final TextView chat){
        try{
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("채팅 입력");
            final EditText txt = new EditText(this);
            txt.setHint("채팅을 입력하세요...");
            int pad = dip2px(10);
            txt.setPadding(pad, pad, pad, pad);
            dialog.setView(txt);
            dialog.setNegativeButton("취소", null);
            dialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    chat.setText(chat.getText() + "[나] " + txt.getText() + "\n");
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            showAnswer(txt.getText().toString(), chat);
                        }
                    }, 1000);
                }
            });
            dialog.show();
        }
        catch(Exception e){
            toast(e.toString());
        }
    }

    public void showAnswer(String que, TextView chat){
        try {
            String cmd = que.split(" ")[0];
            final String cmdData = que.substring(que.indexOf(" ") + 1, que.length());
            que = que.replace(" ", "");
            switch (cmd) {
                case "도움말":
                    siriSay("간절히 바라면 우주가 나서서 도와줍니다.", chat, true);
                    cmdList();
                    break;
                case "검색":
                    siriSay(cmdData + "에 대한 검색 결과입니다.", chat, false);
                    chat.setText(chat.getText()+"[순 Siri] \""+cmdData + "\"에 대한 검색 결과입니다.\n");
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            showWeb(cmdData);
                        }
                    }, 2000);
                    break;
                case "오늘":
                    Calendar day = Calendar.getInstance();
                    siriSay("오늘은 " +day.get(Calendar.YEAR)+"년 "+(day.get(Calendar.MONTH)+1)+"월 "+day.get(Calendar.DATE)+"일입니다.", chat, true);
                    break;
                case "내일":
                    day = Calendar.getInstance();
                    siriSay("내일은 " +day.get(Calendar.YEAR)+"년 "+(day.get(Calendar.MONTH)+1)+"월 "+(day.get(Calendar.DATE)+1)+"일입니다.", chat, true);
                    break;
                case "지금":
                case "시간":
                    day = Calendar.getInstance();
                    siriSay("현재 시각은 " +day.get(Calendar.HOUR)+"시 "+day.get(Calendar.MINUTE)+"분 "+day.get(Calendar.SECOND)+"초입니다.", chat, true);
                    break;
                case "박근혜":
                    siriSay("그건 나의 아바. 아, 아무것도 아닙니다.", chat, false);
                    chat.setText(chat.getText() + "[순 Siri] 그건 나의 아바ㅌ... 아...아무것도 아닙니다.\n");
                    break;
                case "하야":
                case "탄핵":
                    siriSay("순 시리를 종료합니다", chat, false);
                    chat.setText(chat.getText()+"[순 Siri] 순 Siri를 종료합니다.\n");
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            finish();
                        }
                    }, 3000);
                    break;
                default:
                    for (int n = 0; n < chatData.length; n++) {
                        String[] cache = chatData[n].split("::");
                        if (cache[0].equals(que)) {
                            siriSay(cache[1], chat, true);
                            return;
                        }
                    }
                    for (int n = 0; n < chatData2.length; n++) {
                        String[] cache = chatData2[n].split("::");
                        if (checkWorld(cache[0], que)) {
                            siriSay(cache[1], chat, true);
                            return;
                        }
                    }
                    siriSay("인식할 수 없습니다.", chat, true);
            }
        }
        catch(Exception e){
            toast(e.toString());
        }
    }

    private boolean checkWorld(String str1, String str2){
        try{
            if(!str1.contains(",")&&str2.contains(str1)) return true;
            int count = 0;
            String[] data = str1.split(",");
            for(int n=0;n<data.length;n++){
                if(str2.contains(data[n])) count++;
            }
            return count==data.length;
        }
        catch(Exception e){
            toast(e.toString());
        }
        return false;
    }

    public void showWeb(String keyWord){
        try{
            PopupWindow window = new PopupWindow();
            WebView web = new WebView(this);
            WebSettings webSet = web.getSettings();
            webSet.setJavaScriptEnabled(true);
            web.setWebChromeClient(new WebChromeClient());
            web.setWebViewClient(new WebViewClient());
            web.loadUrl("https://m.search.naver.com/search.naver?query=" + keyWord);
            window.setContentView(web);
            window.setFocusable(true);
            window.setWidth(this.getWindowManager().getDefaultDisplay().getWidth());
            window.setHeight(this.getWindowManager().getDefaultDisplay().getHeight());
            window.setBackgroundDrawable(new ColorDrawable(Color.GRAY));
            window.showAtLocation(this.getWindow().getDecorView(), Gravity.CENTER, 0, 0);
        }
        catch(Exception e){
            toast(e.toString());
        }
    }

    public void siriSay(String ans, TextView chat, boolean isLogged){
        try{
            if(isLogged) chat.setText(chat.getText() + "[순 Siri] " + ans+"\n");
            tts.speak(ans, TextToSpeech.QUEUE_FLUSH, null);
        }
        catch(Exception e){
            toast(e.toString());
        }
    }

    private void cmdList(){
        try{
            String[] cmds = {
                    "검색 [검색어] : 해당 검색어에 대한 검색 결과를 웹뷰로 띄웁니다.",
                    "오늘 - 오늘 날짜를 출력합니다.",
                    "하야 - 순Siri를 종료합니다.",
                    "탄핵 - 순Siri를 종료합니다."
            };
            String str = "";
            for(int n=0;n<cmds.length;n++)
                str += " - "+cmds[n]+"\n";
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("도움말");
            dialog.setMessage("간절히 바라면 우주가 나서서 도와줄겁니다.\n\n<명령어 목록>\n" + str + "기타 사용 가능한 것들 : 판사님 저는 죄가 없습니다, 수능 잘 보게 해주세요, 날씨, 제작자, 연설문, 이거 누가 만든거야, 이불 밖은 위험해 등등");
            dialog.setNegativeButton("닫기", null);
            dialog.show();
        }
        catch(Exception e){
            toast(e.toString());
        }
    }

    public int dip2px(int dips){
        return (int)Math.ceil(dips*this.getResources().getDisplayMetrics().density);
    }

    public void toast(String msg){
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        toast.show();
    }

}
