package br.ufc.qx.qdetective;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DenunciaFotoActivity extends Activity implements DatePickerFragment.EscutadorDoDatePickerDialog {

    private static final int CAPTURAR_IMAGEM = 1;
    private EditText usuarioEditTextFoto, descricaoEditTextFoto;
    private TextView latitudeTextViewFoto, longitudeTextViewFoto;
    private Spinner spinnerCategoria;
    private Button salvarButton, dataButtonFoto;
    private DenunciaDAO denunciaDAO;
    private Denuncia denuncia;
    private Date date;
    private Calendar cal;
    private Bundle bundle = null;
    private int id;

    private ImageView fotoDenuncia;
    private Uri uri;

    private LocationManager locationManager;

    @Override
    protected void onResume() {
        super.onResume();
        bundle = this.getIntent().getExtras();
        if (bundle != null) {
            id = bundle.getInt("id", 0);
            if (id > 0) {
                salvarButton.setText("Atualizar Denuncia");
                carregarDados(id);
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_denuncia_foto);
        denunciaDAO = new DenunciaDAO(this);
        denuncia = new Denuncia();
        id = 0;
        usuarioEditTextFoto = findViewById(R.id.usuarioEditTextFoto);
        descricaoEditTextFoto = findViewById(R.id.descricaoEditTextFoto);
        latitudeTextViewFoto = findViewById(R.id.latitudeTextViewFoto);
        longitudeTextViewFoto = findViewById(R.id.longitudeTextViewFoto);
        dataButtonFoto = findViewById(R.id.dataButtonFoto);
        spinnerCategoria = findViewById(R.id.categoriaSpinnerFoto);
        fotoDenuncia = findViewById(R.id.fotoDenuncia);
        salvarButton = findViewById(R.id.buttonSalvarFoto);


        cal = Calendar.getInstance();
        dataButtonFoto.setHint(new SimpleDateFormat("dd/MM/yyyy").format(cal.getTime()));
        fotoDenuncia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                capturarImagem(v);
            }
        });

        getLocationManager();
    }


    public void carregarDados(int id) {
        denuncia = denunciaDAO.buscarDenunciaPorId(id);
        String descricao = denuncia.getDescricao();
        date = denuncia.getData();
        String latitude = denuncia.getLatitude();
        String longitude = denuncia.getLongitude();
        String foto = denuncia.getUriMidia();
        String usuario = denuncia.getUsuario();
        String categoria = denuncia.getCategoria();
        descricaoEditTextFoto.setText(descricao);
        usuarioEditTextFoto.setText(usuario);
        latitudeTextViewFoto.setText(latitude);
        longitudeTextViewFoto.setText(longitude);
        dataButtonFoto.setText(new SimpleDateFormat("dd/MM/yyyy").format(date));
        int pos = 0;
        for(int c = 0; c < (getResources().getStringArray(R.array.listaCategoriaDenuncia)).length ;c++){
            if(categoria == (getResources().getStringArray(R.array.listaCategoriaDenuncia))[c].toString()){
                pos = c;
            }
        }
        spinnerCategoria.setSelection(pos);

        if (foto != null) {
            try {
                uri = Uri.fromFile(getDiretorioDeSalvamento(foto));
                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                fotoDenuncia.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void salvarDenuncia(View view) {
        if (TextUtils.isEmpty(usuarioEditTextFoto.getText().toString().trim())) {
            usuarioEditTextFoto.setError("Campo obrigatório.");
            return;
        }

        if (TextUtils.isEmpty(latitudeTextViewFoto.getText().toString().trim())) {
            latitudeTextViewFoto.setError("Campo obrigatório.");
            return;
        }

        if (TextUtils.isEmpty(descricaoEditTextFoto.getText().toString().trim())) {
            descricaoEditTextFoto.setError("Campo obrigatório.");
            return;
        }
        String usuario = usuarioEditTextFoto.getText().toString();
        String descricao = descricaoEditTextFoto.getText().toString();
        String latitude = latitudeTextViewFoto.getText().toString();
        String longitude = longitudeTextViewFoto.getText().toString();
        String foto = null;
        String cadegoria = spinnerCategoria.getSelectedItem().toString();

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

    @Override
    public void onDateSelectedClick(Date date) {
        dataButtonFoto.setText(new SimpleDateFormat("dd/MM/yyyy").format(date));
        this.date = date;
    }

    public void cadastrarData(View view) {
        DatePickerFragment datePickerFragment = new DatePickerFragment();
        datePickerFragment.show(this.getFragmentManager(), "cadastroData");
    }

    private void iniciarCapturaDeFotos() {
        try {
            String nomeArquivo = (denuncia.getUriMidia() == null) ? System.currentTimeMillis() + ".jpg" : denuncia.getUriMidia();
            uri = setArquivoImagem(nomeArquivo);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            startActivityForResult(intent, CAPTURAR_IMAGEM);
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao iniciar a câmera.", Toast.LENGTH_LONG).show();
        }
    }

    private Uri setArquivoImagem(String nomeArquivo) {
        File pathDaImagem = getDiretorioDeSalvamento(nomeArquivo);
        Uri uri = null;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                String authority = "br.ufc.qx.qdetective.fileprovider";
                uri = FileProvider.getUriForFile(this.getApplicationContext(), authority, pathDaImagem);
            } catch (Exception e) {
                Toast.makeText(this, "Erro a acessar o FileProvider.", Toast.LENGTH_LONG).show();
            }
        } else {
            uri = Uri.fromFile(pathDaImagem);
        }
        return uri;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURAR_IMAGEM) {
            if (resultCode == RESULT_OK) {
                RecarregarImagem recarregarImagem = new RecarregarImagem();
                recarregarImagem.execute();
            } else {
                Toast.makeText(this, "Imagem não capturada!", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void capturarImagem(View v) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            getPermissoes();
        } else {
            iniciarCapturaDeFotos();
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
                    getLocationManager();
                    iniciarCapturaDeFotos();
                } else {
                    Toast.makeText(this, "Sem permissão.", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }


    private void getPermissoes() {
        boolean camera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean leitura = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        boolean escrita = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        if (camera && leitura && escrita) {
            iniciarCapturaDeFotos();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        }
    }

    class RecarregarImagem extends AsyncTask<Void, Integer, Bitmap> {
        @Override
        protected Bitmap doInBackground(Void... voids) {
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                int bmpWidth = bitmap.getWidth();
                int bmpHeight = bitmap.getHeight();
                Matrix matrix = new Matrix();
                matrix.postRotate(0);
                Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bmpWidth, bmpHeight, matrix, true);
                denuncia.setUriMidia(getDiretorioDeSalvamento(uri.toString()).getPath());
                return resizedBitmap;
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Imagem não encontrada!", Toast.LENGTH_LONG).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            fotoDenuncia.setImageBitmap(bitmap);
        }
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

            latitudeTextViewFoto.setText(latitudeStr);
            longitudeTextViewFoto.setText(longitudeStr);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

}
