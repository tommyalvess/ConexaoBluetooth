package br.com.fiap.conexaobluetooth;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    Spinner spDispositivos;
    EditText txtInformacao;

    private ProgressDialog mProgressDialog;

    BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
    BluetoothSocket soquete = null;
    OutputStream saida = null;
    Set<BluetoothDevice> dispositivoPareados;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spDispositivos = (Spinner)findViewById(R.id.spDispositivos);
        txtInformacao = (EditText)findViewById(R.id.txtInformacao);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        arrayAdapter.add("Selecionar um dispositivo");

        //verificando se o aparelho tem bluetooth.
        if( bluetooth != null){

            //Verificando se está ativo e manda ativar
            if (!bluetooth.isEnabled()){

                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                int REQUEST_ENABLE_BT = 1;
                startActivityForResult(enableBluetooth, REQUEST_ENABLE_BT);

            }
            //Ele pega todos os dispositivos pareados e retorna o resultadpo na variavel.
            dispositivoPareados = bluetooth.getBondedDevices();

            if (dispositivoPareados.size()>0){
                for (BluetoothDevice item : dispositivoPareados){
                    arrayAdapter.add(item.getName());
                }
            }

        }

        spDispositivos.setAdapter(arrayAdapter );
    }

    public void Enviar(View view) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {

        BluetoothAsyncTask bluetoothAsyncTask = new BluetoothAsyncTask();
        bluetoothAsyncTask.execute(txtInformacao.getText().toString(), spDispositivos.getSelectedItem().toString(), txtInformacao.getText().toString());

    }

    private class BluetoothAsyncTask  extends AsyncTask<String, Void,Boolean>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mProgressDialog = ProgressDialog.show(MainActivity.this, "Aguarde...", "Bluetooth!");

        }

        @Override
        protected Boolean doInBackground(String... params) {

            for (BluetoothDevice item : dispositivoPareados){
                if (params[1].equalsIgnoreCase(item.getName())){
                    try {
                        BluetoothDevice dispositivoRemoto = bluetooth.getRemoteDevice(item.getAddress());
                        soquete = criarSoqueteBluetooth(dispositivoRemoto);
                        soquete.connect();

                        bluetooth.cancelDiscovery();
                        saida = soquete.getOutputStream();

                        byte[] buffer = params[2].getBytes();
                        saida.write(buffer);

                        saida.close();
                        soquete.close();

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

            return null;
        }
    }

    private BluetoothSocket criarSoqueteBluetooth(BluetoothDevice dispositivo) throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method metodo;
        BluetoothSocket tmpSoquete = null;

        try {
            metodo = dispositivo.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
            tmpSoquete = (BluetoothSocket) metodo.invoke(dispositivo, 1);
        }finally {
            return tmpSoquete;
        }
    }
}
