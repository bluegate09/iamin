package idv.tfp10101.iamin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.Navigation;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.TransactionInfo;
import com.google.android.gms.wallet.WalletConstants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import idv.tfp10101.iamin.member.Member;
import idv.tfp10101.iamin.member.MemberControl;
import idv.tfp10101.iamin.network.RemoteAccess;
import tech.cherri.tpdirect.api.TPDCard;
import tech.cherri.tpdirect.api.TPDConsumer;
import tech.cherri.tpdirect.api.TPDGooglePay;
import tech.cherri.tpdirect.api.TPDMerchant;
import tech.cherri.tpdirect.api.TPDServerType;
import tech.cherri.tpdirect.api.TPDSetup;
import tech.cherri.tpdirect.callback.TPDGooglePayGetPrimeSuccessCallback;
import tech.cherri.tpdirect.callback.dto.TPDCardInfoDto;
import tech.cherri.tpdirect.callback.dto.TPDMerchantReferenceInfoDto;

public class TappayActivity extends AppCompatActivity {
    private static final String TAG = "TAG_TappayActivity";
    public static final String TAPPAY_DOMAIN_SANDBOX = "https://sandbox.tappaysdk.com/";
    public static final String TAPPAY_PAY_BY_PRIME_URL = "tpc/payment/pay-by-prime";
    public static final TPDCard.CardType[] CARD_TYPES = new TPDCard.CardType[]{
            TPDCard.CardType.Visa
            , TPDCard.CardType.MasterCard
            , TPDCard.CardType.JCB
            , TPDCard.CardType.AmericanExpress
    };
    private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 101;
    private TPDGooglePay tpdGooglePay;
    private RelativeLayout btBuy;
    private TextView tvTotalAmount;
    private TextView tvResult;
    private TextView tvCardInfo;
    private PaymentData paymentData;
    private Button btConfirm;
    private Integer totalPrice;
    private Integer memberOderId;
    private Member member;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tappay);
        member = MemberControl.getInstance();
        Bundle bundle = getIntent().getExtras();
        totalPrice = (Integer) bundle.getSerializable("totalPrice");
        memberOderId = (Integer) bundle.getSerializable("memberOderId");
        Log.d(TAG, "SDK version is " + TPDSetup.getVersion());
        handleViews();
        // 使用TPDSetup設定環境。每個設定值出處參看strings.xml
        TPDSetup.initInstance(this,
                Integer.parseInt(getString(R.string.TapPay_AppID)),
                getString(R.string.TapPay_AppKey),
                TPDServerType.Sandbox);

        prepareGooglePay();
    }



    public void prepareGooglePay() {
        TPDMerchant tpdMerchant = new TPDMerchant();
        // 設定商店名稱
        tpdMerchant.setMerchantName(getString(R.string.TapPay_MerchantName));
        // 設定允許的信用卡種類
        tpdMerchant.setSupportedNetworks(CARD_TYPES);
        // 設定客戶填寫項目
        TPDConsumer tpdConsumer = new TPDConsumer();
        // 不需要電話號碼
        tpdConsumer.setPhoneNumberRequired(false);
        // 不需要運送地址
        tpdConsumer.setShippingAddressRequired(false);
        // 不需要Email
        tpdConsumer.setEmailRequired(false);

        tpdGooglePay = new TPDGooglePay(this, tpdMerchant, tpdConsumer);
        // 檢查user裝置是否支援Google Pay
        tpdGooglePay.isGooglePayAvailable((isReadyToPay, msg) -> {
            Log.d(TAG, "Pay with Google availability : " + isReadyToPay);
            if (isReadyToPay) {
                btBuy.setEnabled(true);
            } else {
                btBuy.setEnabled(false);
                tvResult.setText(R.string.textCannotUseGPay);
            }
        });
    }

    private void handleViews() {
        tvTotalAmount = findViewById(R.id.totalAmountTV);
        tvTotalAmount.setText(String.valueOf(totalPrice));
        btBuy = findViewById(R.id.btBuy);
        // 先將Google Pay按鈕設定為不可按，
        // 直到等會執行prepareGooglePay()確定可使用時再改成可按
        btBuy.setEnabled(false);
        btBuy.setOnClickListener(v -> {
            // 跳出user資訊視窗讓user確認，確認後會呼叫onActivityResult()
            tpdGooglePay.requestPayment(TransactionInfo.newBuilder()
                    .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
                    // 消費總金額
                    .setTotalPrice(String.valueOf(totalPrice))
                    // 設定幣別
                    .setCurrencyCode("TWD")
                    .build(), LOAD_PAYMENT_DATA_REQUEST_CODE);
        });
        tvCardInfo = findViewById(R.id.tvCardInfo);
        btConfirm = findViewById(R.id.btConfirm);
        btConfirm.setEnabled(false);
        btConfirm.setOnClickListener(v -> getPrimeFromTapPay(paymentData));
        tvResult = findViewById(R.id.tvResult);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOAD_PAYMENT_DATA_REQUEST_CODE) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    btConfirm.setEnabled(true);
                    // 取得支付資訊
                    paymentData = PaymentData.getFromIntent(data);
                    if (paymentData != null) {
                        // 取得支付資訊中的信用卡資訊並顯示
                        showCardInfo(paymentData);
                    }
                    break;
                case Activity.RESULT_CANCELED:
                    btConfirm.setEnabled(false);
                    tvResult.setText(R.string.textCanceled);
                    break;
                case AutoResolveHelper.RESULT_ERROR:
                    btConfirm.setEnabled(false);
                    Status status = AutoResolveHelper.getStatusFromIntent(data);
                    if (status != null) {
                        String text = "status code: " + status.getStatusCode() +
                                " , message: " + status.getStatusMessage();
                        Log.d(TAG, text);
                        tvResult.setText(text);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 只取得支付資訊當中的信用卡資訊並顯示
     */
    private void showCardInfo(PaymentData paymentData) {
        Gson gson = new Gson();
        // 讀出paymentData.toJson()
        JsonObject paymentDataJO = gson.fromJson(paymentData.toJson(), JsonObject.class);
//        Log.d(TAG, paymentData.toJson());
        String cardDescription = paymentDataJO.get("paymentMethodData").getAsJsonObject()
                .get("description").getAsString();
        tvCardInfo.setText(cardDescription);
    }

    //  GetPrime
    private void getPrimeFromTapPay(PaymentData paymentData) {
        showProgressDialog();
        tpdGooglePay.getPrime(
                paymentData,
                new TPDGooglePayGetPrimeSuccessCallback() {
                    @Override
                    public void onSuccess(String prime, TPDCardInfoDto tpdCardInfoDto, TPDMerchantReferenceInfoDto tpdMerchantReferenceInfoDto) {
                        TappayActivity.this.hideProgressDialog();
                        String text = "Your prime is " + prime + "\n\n"
                            /* 手機得到prime後，一般會傳給商家server端再呼叫payByPrime方法提交給TapPay，以確認這筆訂單
                               現在為了方便，手機直接提交給TapPay */
                                + generatePayByPrimeForSandBox(prime,
                                getString(R.string.TapPay_PartnerKey),
                                getString(R.string.TapPay_MerchantID), totalPrice, memberOderId, member.getNickname(), member.getEmail());
                        Log.d(TAG, prime);
                        // TODO: Check
                        Intent intent = new Intent(TappayActivity.this, MainActivity.class);
                        intent.putExtra("data", "Home_Fragment");
                        startActivity(intent);
                    }
                },
                (status, reportMsg) -> {
                    hideProgressDialog();
                    Log.d(TAG, "=============>" + reportMsg + status);
                    Toast.makeText(this, reportMsg, Toast.LENGTH_SHORT).show();
                });
    }

    // 將交易資訊送至TapPay測試區
    public static String generatePayByPrimeForSandBox(String prime, String partnerKey, String merchantID, Integer totalPrice, Integer memberOderId, String name, String email) {
        JsonObject paymentJO = new JsonObject();
        paymentJO.addProperty("partner_key", partnerKey);
        paymentJO.addProperty("prime", prime);
        paymentJO.addProperty("merchant_id", merchantID);
        paymentJO.addProperty("amount", totalPrice);
        paymentJO.addProperty("currency", "TWD");
        paymentJO.addProperty("order_number", memberOderId);
        paymentJO.addProperty("details", "");

        JsonObject cardHolderJO = new JsonObject();
        cardHolderJO.addProperty("name", name);
        cardHolderJO.addProperty("phone_number", "");
        cardHolderJO.addProperty("email", email);

        paymentJO.add("cardholder", cardHolderJO);

        // TapPay測試區網址
        String url = TAPPAY_DOMAIN_SANDBOX + TAPPAY_PAY_BY_PRIME_URL;
        return RemoteAccess.getRemotePayData(url, paymentJO.toString(), partnerKey);
    }


    public ProgressDialog mProgressDialog;

    protected void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setMessage("Loading...");
        }

        mProgressDialog.show();
    }

    protected void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}
