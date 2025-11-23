package avito.testtask.data.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class SimpleSecretManager(context: Context) {

    private val sharedPreferences by lazy {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        EncryptedSharedPreferences.create(
            "cloud_secrets",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveSecrets(accessKey: String, secretKey: String, bucketName: String) {
        sharedPreferences.edit()
            .putString("access_key", accessKey)
            .putString("secret_key", secretKey)
            .putString("bucket_name", bucketName)
            .apply()
    }

    fun getAccessKey(): String? = sharedPreferences.getString("access_key", null)
    fun getSecretKey(): String? = sharedPreferences.getString("secret_key", null)
    fun getBucketName(): String? = sharedPreferences.getString("bucket_name", null)

    fun hasSecrets(): Boolean = getAccessKey() != null && getSecretKey() != null && getBucketName() != null
}