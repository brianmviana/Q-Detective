package br.ufc.qx.qdetective;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DetetalheActivity extends Activity{

    private int id;
    private Bundle bundle;
    private Denuncia denuncia;
    private Date date;
    DenunciaDAO denunciaDAO;


    private String urlBase = "http://maps.googleapis.com/maps/api/staticmap" +
            "?size=400x400&sensor=true&markers=color:red|%s,%s";

    private TextView descricaoDetalhes,usuarioDetalhes , categoriaDetalhes,dataDetalhes;
    private ImageView imagemDetalhes;
    private WebView mapaDetalhes;


    @Override
    protected void onResume() {
        super.onResume();
        bundle = this.getIntent().getExtras();
        if (bundle != null) {
            id = bundle.getInt("id", 0);
            if (id > 0) {
                carregarDados(id);
            }
        }
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detetalhe);

        denunciaDAO = new DenunciaDAO(this);

        descricaoDetalhes = findViewById(R.id.descricaoDetalhes);
        imagemDetalhes = findViewById(R.id.imagemDetalhes);
        usuarioDetalhes = findViewById(R.id.usuarioDetalhes);
        categoriaDetalhes = findViewById(R.id.categoriaDetalhes);
        dataDetalhes = findViewById(R.id.dataDetalhes);
        mapaDetalhes = findViewById(R.id.mapaDetalhes);

        mapaDetalhes.getSettings().setJavaScriptEnabled(true);
        mapaDetalhes.getSettings().setSupportZoom(true);
        mapaDetalhes.getSettings().setBuiltInZoomControls(true);
        mapaDetalhes.setBackgroundColor(Color.parseColor("#FFFFFF"));
        mapaDetalhes.getSettings().setUseWideViewPort(true);
        mapaDetalhes.getSettings().setLoadWithOverviewMode(false);


    }


    public void carregarDados(int id) {
        denuncia = denunciaDAO.buscarDenunciaPorId(id);
        String descricao = denuncia.getDescricao();
        date = denuncia.getData();
        String latitude = (denuncia.getLatitude()).toString();
        String longitude = denuncia.getLongitude().toString();
        String foto = denuncia.getUriMidia();
        String usuario = denuncia.getUsuario();
        String categoria = denuncia.getCategoria();
        descricaoDetalhes.setText(descricao);
        usuarioDetalhes.setText(usuario);
        categoriaDetalhes.setText(categoria);
        dataDetalhes.setText(new SimpleDateFormat("dd/MM/yyyy").format(date));

        String url = String.format(urlBase, latitude, longitude);
        mapaDetalhes.loadUrl(url);

        if (foto != null) {
            try {
                Uri uri = Uri.fromFile(getDiretorioDeSalvamento(foto));
                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                imagemDetalhes.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
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


}
