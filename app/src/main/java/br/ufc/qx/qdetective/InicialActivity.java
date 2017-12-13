package br.ufc.qx.qdetective;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class InicialActivity extends Activity {

    private Button addDenunciaFoto;
    private Button addDenunciaVideo;
    private Button listarDenuncia;
//    private Button local;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicial);

        addDenunciaFoto = findViewById(R.id.addDenunciaFoto);
        addDenunciaVideo = findViewById(R.id.addDenunciaVideo);
        listarDenuncia = findViewById(R.id.listarDenunciaInicial);
//        local = findViewById(R.id.location);

        adicionarDenunciaFoto();
        adicionarDenunciaVideo();
        listarDenuncia();
    }

    private void adicionarDenunciaFoto(){
        addDenunciaFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InicialActivity.this, DenunciaFotoActivity.class);
                startActivity(intent);
            }
        });
    }
    private void adicionarDenunciaVideo(){
        addDenunciaVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InicialActivity.this, DenunciaVideoActivity.class);
                startActivity(intent);
            }
        });
    }

    private void listarDenuncia(){
        listarDenuncia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InicialActivity.this, ListarActivity.class);
                startActivity(intent);
            }
        });
    }

}
