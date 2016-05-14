package heron.cordova.plugin.baidutts;

import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.baidu.tts.answer.auth.AuthInfo;
import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class BaiduTts extends CordovaPlugin implements SpeechSynthesizerListener {
    public static final String TAG = "baiduTts";
    private SpeechSynthesizer mSpeechSynthesizer;
    private String mSampleDirPath;
    private static final String SAMPLE_DIR_NAME = "baiduTTS";
    private static final String SPEECH_FEMALE_MODEL_NAME = "bd_etts_speech_female.dat";
    private static final String SPEECH_MALE_MODEL_NAME = "bd_etts_speech_male.dat";
    private static final String TEXT_MODEL_NAME = "bd_etts_text.dat";
    private static final String LICENSE_FILE_NAME = "temp_license";
    private static final String ENGLISH_SPEECH_FEMALE_MODEL_NAME = "bd_etts_speech_female_en.dat";
    private static final String ENGLISH_SPEECH_MALE_MODEL_NAME = "bd_etts_speech_male_en.dat";
    private static final String ENGLISH_TEXT_MODEL_NAME = "bd_etts_text_en.dat";

    protected CallbackContext currentCallbackContext;

    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();
        Toast.makeText(cordova.getActivity(), "初始化！", Toast.LENGTH_SHORT)
                .show();
        initialEnv();
        initialTts();

        Log.d(TAG, "plugin initialized.");
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        currentCallbackContext = callbackContext;

        JSONObject options = new JSONObject();
        try {
            options = args.getJSONObject(0);
        } catch (JSONException e) {
            //Log.v(TAG, "options 未传入");
        }

        if (action.equals("speak")) {
            String text = options.getString("text");
            String volume = options.getString("volume");
            String speed = options.getString("speed");
            String pitch = options.getString("pitch");
            int result = speak(text, volume, speed, pitch);
            if (result < 0) {
                Toast.makeText(cordova.getActivity(), "speak error！code:" + result, Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        if (action.equals("stop")) {
            stop();
            return true;
        }
        return false;
    }

    public int speak(String text, String volume, String speed, String pitch) {
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME, volume);//音量,范围[0-9]
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, speed);//语速,范围[0-9]
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_PITCH, pitch);//语调,范围[0-9]
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0");//0 (普通女声), 1 (普通男声), 2 (特别男声), 3 (情感男声)
        //MIX_MODE_DEFAULT(mi x 模式下,wifi 使用在线合 成,非 wifi 使用离线合成); MIX_MODE_HIGH_SPEED _NETWORK(mix 模式下, wifi,4G,3G 使用在线合成, 其他使用离线合成);
        //MIX_MODE_HIGH_SPEED_SYNTHESIZE(mix 模 式 下,在线返回速度如果慢 (超时,一般为 1.2 秒)直 接切换离线,适用于网络环 境较差的情况)
        //MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI(mix 模式下,仅 wifi 使用在线合 成,返回速度如果慢(超时, 一般为 1.2 秒)直接切换离
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_AUDIO_ENCODE, SpeechSynthesizer.AUDIO_ENCODE_AMR);
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_AUDIO_RATE, SpeechSynthesizer.AUDIO_BITRATE_AMR_15K85);
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOCODER_OPTIM_LEVEL, "2");
        return mSpeechSynthesizer.speak(text);
    }

    public void stop() {
        mSpeechSynthesizer.stop();
    }

    /**
     * 将sample工程需要的资源文件拷贝到SD卡中使用（授权文件为临时授权文件，请注册正式授权）
     *
     * @param isCover 是否覆盖已存在的目标文件
     * @param source
     * @param dest
     */
    private void copyFromAssetsToSdcard(boolean isCover, String source, String dest) {
        File file = new File(dest);
        if (isCover || (!isCover && !file.exists())) {
            InputStream is = null;
            FileOutputStream fos = null;
            try {
                is = cordova.getActivity().getApplicationContext().getResources().getAssets().open(source);
                String path = dest;
                fos = new FileOutputStream(path);
                byte[] buffer = new byte[1024];
                int size = 0;
                while ((size = is.read(buffer, 0, 1024)) >= 0) {
                    fos.write(buffer, 0, size);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void initialEnv() {
        if (mSampleDirPath == null) {
            String sdcardPath = Environment.getExternalStorageDirectory().toString();
            mSampleDirPath = sdcardPath + "/" + SAMPLE_DIR_NAME;
        }
        makeDir(mSampleDirPath);
        copyFromAssetsToSdcard(false, SPEECH_FEMALE_MODEL_NAME, mSampleDirPath + "/" + SPEECH_FEMALE_MODEL_NAME);
        copyFromAssetsToSdcard(false, SPEECH_MALE_MODEL_NAME, mSampleDirPath + "/" + SPEECH_MALE_MODEL_NAME);
        copyFromAssetsToSdcard(false, TEXT_MODEL_NAME, mSampleDirPath + "/" + TEXT_MODEL_NAME);
        //copyFromAssetsToSdcard(false, LICENSE_FILE_NAME, mSampleDirPath + "/" + LICENSE_FILE_NAME);
        copyFromAssetsToSdcard(false, "english/" + ENGLISH_SPEECH_FEMALE_MODEL_NAME, mSampleDirPath + "/"
                + ENGLISH_SPEECH_FEMALE_MODEL_NAME);
        copyFromAssetsToSdcard(false, "english/" + ENGLISH_SPEECH_MALE_MODEL_NAME, mSampleDirPath + "/"
                + ENGLISH_SPEECH_MALE_MODEL_NAME);
        copyFromAssetsToSdcard(false, "english/" + ENGLISH_TEXT_MODEL_NAME, mSampleDirPath + "/"
                + ENGLISH_TEXT_MODEL_NAME);
    }

    private void makeDir(String dirPath) {
        File file = new File(dirPath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    private void initialTts() {
        this.mSpeechSynthesizer = SpeechSynthesizer.getInstance();
        this.mSpeechSynthesizer.setContext(cordova.getActivity().getApplicationContext());
        this.mSpeechSynthesizer.setSpeechSynthesizerListener(this);
        // 文本模型文件路径 (离线引擎使用)
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, mSampleDirPath + "/"
                + TEXT_MODEL_NAME);
        // 声学模型文件路径 (离线引擎使用)
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, mSampleDirPath + "/"
                + SPEECH_FEMALE_MODEL_NAME);
        // 本地授权文件路径,如未设置将使用默认路径.设置临时授权文件路径，LICENCE_FILE_NAME请替换成临时授权文件的实际路径，仅在使用临时license文件时需要进行设置，如果在[应用管理]中开通了离线授权，不需要设置该参数，建议将该行代码删除（离线引擎）
        //this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_LICENCE_FILE, mSampleDirPath + "/"
        //        + LICENSE_FILE_NAME);
        // 请替换为语音开发者平台上注册应用得到的App ID (离线授权)
        this.mSpeechSynthesizer.setAppId(Keys.APP_ID);
        // 请替换为语音开发者平台注册应用得到的apikey和secretkey (在线授权)
        this.mSpeechSynthesizer.setApiKey(Keys.API_KEY, Keys.SECRET_KEY);
        // 发音人（在线引擎），可用参数为0,1,2,3。。。（服务器端会动态增加，各值含义参考文档，以文档说明为准。0--普通女声，1--普通男声，2--特别男声，3--情感男声。。。）
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0");
        // 设置Mix模式的合成策略
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
        // 授权检测接口(可以不使用，只是验证授权是否成功)
        AuthInfo authInfo = this.mSpeechSynthesizer.auth(TtsMode.MIX);
        if (authInfo.isSuccess()) {
            Log.i(TAG, "auth success");
        } else {
            String errorMsg = authInfo.getTtsError().getDetailMessage();
            Log.i(TAG, "auth failed errorMsg=" + errorMsg);
        }
        // 初始化tts
        mSpeechSynthesizer.initTts(TtsMode.MIX);
        // 加载离线英文资源（提供离线英文合成功能）
        int result =
                mSpeechSynthesizer.loadEnglishModel(mSampleDirPath + "/" + ENGLISH_TEXT_MODEL_NAME, mSampleDirPath
                        + "/" + ENGLISH_SPEECH_FEMALE_MODEL_NAME);
        Log.i(TAG, "loadEnglishModel result=" + result);
    }


    @Override
    public void onSynthesizeStart(String s) {

    }

    @Override
    public void onSynthesizeDataArrived(String s, byte[] bytes, int i) {

    }

    @Override
    public void onSynthesizeFinish(String s) {

    }

    @Override
    public void onSpeechStart(String s) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("event", "onStart");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // send no result and keep callback
        PluginResult result = new PluginResult(PluginResult.Status.OK, jsonObject);
        result.setKeepCallback(true);
        currentCallbackContext.sendPluginResult(result);
    }

    @Override
    public void onSpeechProgressChanged(String s, int i) {

    }

    @Override
    public void onSpeechFinish(String s) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("event", "onStop");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // send no result and keep callback
        PluginResult result = new PluginResult(PluginResult.Status.OK, jsonObject);
        result.setKeepCallback(true);
        currentCallbackContext.sendPluginResult(result);
    }

    @Override
    public void onError(String s, SpeechError speechError) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("event", "onStop");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // send no result and keep callback
        PluginResult result = new PluginResult(PluginResult.Status.ERROR, jsonObject);
        result.setKeepCallback(true);
        currentCallbackContext.sendPluginResult(result);
    }
}
