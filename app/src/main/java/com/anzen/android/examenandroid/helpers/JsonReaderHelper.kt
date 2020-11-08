package com.anzen.android.examenandroid.helpers

import android.content.Context
import com.anzen.android.examenandroid.R
import com.anzen.android.examenandroid.utils.EcobiciContentModel
import com.anzen.android.examenandroid.utils.ResponseListener
import com.google.gson.Gson

class JsonReaderHelper(private val context: Context) {

    fun getInfoBikes(responseListener : ResponseListener<List<EcobiciContentModel>>) {
        val jsonBikes = context.resources.openRawResource(R.raw.bikes)
            .bufferedReader().use { it.readText() }

        val bikes = Gson().fromJson(jsonBikes, Array<EcobiciContentModel>::class.java).toList()

        //TODO PARSEAR EL json que está en jsonBikes y devolverlo con la interfaz ResponseListener
        // con el método onSuccess para el model que se defina y con el onError por si existe una excepcion al parsear,
        // retornando un string para el error y que se pueda mostrar al usuario.
        //

        responseListener.onSuccess(/* TODO RESPUESTA EN MODELO PARSEADO */ bikes)
        responseListener.onError(/* TODO RESPUESTA EN CASO DE ERROR AL PARSEAR */ "Ocurrió un error!")
    }
}