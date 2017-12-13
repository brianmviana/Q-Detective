package br.ufc.qx.qdetective;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DenunciaVideoActivity extends Activity implements DatePickerFragment.EscutadorDoDatePickerDialog {

    private static final int CAPTURAR_VIDEO = 1;
    private Uri uri;
    private Boolean possuiCartaoSD = false;
    private VideoView videoView;
    private Button filmar;
    private Button dataButtonVideo;
    private Date date;
    private TextView latitudeText, longitudeText;
    private EditText usuarioTextVideo, descricaoTextVideo;
    private Spinner spinnerCategoriaVideo;
    private LocationManager locationManager;

    private Denuncia denuncia;
    private DenunciaDAO denunciaDAO;

    private int id;

    private Bundle bundle;

    @Override
    protected void onResume() {
        super.onResume();
        bundle = this.getIntent().getExtras();
        if (bundle != null) {
            id = bundle.getInt("id", 0);
            if (id > 0) {
//                salvarButton.setText("Atualizar Denuncia");
//                carregarDados(id);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_denuncia_video);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        videoView = findViewById(R.id.videoDenuncia);
        possuiCartaoSD = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        filmar = findViewById(R.id.buttonFilmar);
        dataButtonVideo = findViewById(R.id.dataButtonVideo);
        latitudeText = findViewById(R.id.latitudeTextViewVideo);
        longitudeText = findViewById(R.id.longitudeTextViewVideo);
        usuarioTextVideo = findViewById(R.id.usuarioEditTextVideo);
        descricaoTextVideo = findViewById(R.id.descricaoEditTextVideo);
        spinnerCategoriaVideo = findViewById(R.id.categoriaSpinnerVideo);

        denunciaDAO = new DenunciaDAO(this);

        filmar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                capturarVideo(view);
                visualizarVideo(view);
            }
        });

        getLocationManager();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURAR_VIDEO) {
            if (resultCode == RESULT_OK) {
                String msg = "Vídeo gravado em " + data.getDataString();
                uri = data.getData();
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Video não gravado!", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void visualizarVideo(View v) {
        videoView.setVideoURI(uri);
        MediaController mc = new MediaController(this);
        videoView.setMediaController(mc);
        videoView.start();
    }

    public void capturarVideo(View v) {
        getPermissoes();
    }


    private void getPermissoes() {
        String CAMERA = Manifest.permission.CAMERA;
        String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        String READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
        int PERMISSION_GRANTED = PackageManager.PERMISSION_GRANTED;

        boolean permissaoCamera = ActivityCompat.checkSelfPermission(this, CAMERA) == PERMISSION_GRANTED;
        boolean permissaoEscrita = ActivityCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED;
        boolean permissaoLeitura = ActivityCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED;

        if (permissaoCamera && permissaoEscrita && permissaoLeitura) {
            iniciarGravacaoDeVideo();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{CAMERA, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, 1);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    iniciarGravacaoDeVideo();
                } else {
                    Toast.makeText(this, "Sem permissão para uso de câmera.", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }


    private void iniciarGravacaoDeVideo() {
        try {
            setArquivoVideo();
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
            intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);
            startActivityForResult(intent, CAPTURAR_VIDEO);
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao iniciar a câmera.", Toast.LENGTH_LONG).show();
        }
    }


    private void setArquivoVideo() {
        File diretorio = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        if (!possuiCartaoSD) {
            diretorio = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        }

        File pathVideo = new File(diretorio + "/" + System.currentTimeMillis() + ".mp4");

        if (android.os.Build.VERSION.SDK_INT >= 23) {
            String authority = this.getApplicationContext().getPackageName() + ".fileprovider";
            uri = FileProvider.getUriForFile(this, authority, pathVideo);
        } else {
            uri = Uri.fromFile(pathVideo);
        }
    }


    @Override
    public void onDateSelectedClick(Date date) {
        dataButtonVideo.setText(new SimpleDateFormat("dd/MM/yyyy").format(date));
        this.date = date;
    }

    public void cadastrarDataVideo(View view) {
        DatePickerFragment datePickerFragment = new DatePickerFragment();
        datePickerFragment.show(this.getFragmentManager(), "cadastroData");
    }


    public void salvarDenunciaVideo(View view) {
        if (TextUtils.isEmpty(usuarioTextVideo.getText().toString().trim())) {
            usuarioTextVideo.setError("Campo obrigatório.");
            return;
        }

        if (TextUtils.isEmpty(latitudeText.getText().toString().trim())) {
            latitudeText.setError("Campo obrigatório.");
            return;
        }
        if (TextUtils.isEmpty(longitudeText.getText().toString().trim())) {
            longitudeText.setError("Campo obrigatório.");
            return;
        }

        if (TextUtils.isEmpty(descricaoTextVideo.getText().toString().trim())) {
            descricaoTextVideo.setError("Campo obrigatório.");
            return;
        }
        String usuario = usuarioTextVideo.getText().toString();
        String descricao = descricaoTextVideo.getText().toString();
        Double latitude = Double.parseDouble(latitudeText.getText().toString());
        Double longitude = Double.parseDouble(longitudeText.getText().toString());
        String foto = null;
        String cadegoria = spinnerCategoriaVideo.getSelectedItem().toString();

        if (uri != null) {
            foto = getDiretorioDeSalvamento(uri.toString()).toString();
        }
        denuncia = new Denuncia(id, descricao, date, latitude, longitude, foto, usuario, cadegoria);
        if (id > 0) {
            denunciaDAO.atualizarDenuncia(denuncia);
        } else {
            denunciaDAO.inserirDenuncia(denuncia);
        }
        finish();
    }


    private File getDiretorioDeSalvamento(String nomeArquivo) {
        if (nomeArquivo.contains("/")) {
            int beginIndex = nomeArquivo.lastIndexOf("/") + 1;
            nomeArquivo = nomeArquivo.substring(beginIndex);
        }
        File diretorio = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File pathDaImagem = new File(diretorio, nomeArquivo);
        return pathDaImagem;
    }

    private void getLocationManager() {
        Listener listener = new Listener();

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        long tempoAtualizacao = 0;
        float distancia = 0;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.INTERNET},
                    1);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, tempoAtualizacao, distancia, listener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, tempoAtualizacao, distancia, listener);
    }

    class Listener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            String latitudeStr = String.valueOf(location.getLatitude());
            String longitudeStr = String.valueOf(location.getLongitude());

            latitudeText.setText(latitudeStr);
            longitudeText.setText(longitudeStr);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}
    }

}

