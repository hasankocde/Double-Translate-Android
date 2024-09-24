package com.two.translate;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

/**
 * Bu etkinlik PROCESS_TEXT yöntemini alır ve bu metni PROCESS_TEXT içeriğini çağırarak çağırır.
 * Farklı bir etikete sahip olmasına izin vermek için ayrıldı ve ayrıca bu özellik yalnızca Android 6.0 ve üstü içindir
 */
@TargetApi(Build.VERSION_CODES.M)
public class Process extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Seçilen metni al
        CharSequence selectedText = getIntent().getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);

        // Metni işle
        processText(selectedText.toString());
    }

    private void processText(String selectedText) {
        // İlgili metni işleme ve çeviri etkinliğine iletilmek üzere TranslatorActivity'yi çağırma
        Intent intent = new Intent(this, TranslatorActivity.class);
        intent.putExtra(Intent.EXTRA_TEXT, selectedText);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Yeni bir görev olarak başlatma (geçerli etkinliğe bağlı olmayan)
        startActivity(intent);

        // Sonucu iletilir (salt okunur özelliğini görmezden geliriz, gerekli değil)
        Intent resultIntent = new Intent();
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
