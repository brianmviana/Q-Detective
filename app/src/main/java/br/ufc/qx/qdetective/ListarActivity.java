package br.ufc.qx.qdetective;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.io.File;
import java.util.List;
import java.util.Map;


public class ListarActivity extends Activity implements
        AdapterView.OnItemClickListener,
        MenuDialogFragment.NotificarEscutadorDoDialog, SimpleAdapter.ViewBinder {

    private final String url = "http://35.193.98.124/QDetective/";
    private boolean permisaoInternet = false;
    private SimpleAdapter adapter;
    private ListView listView;
    private DenunciaDAO denunciaDAO;
    private List<Map<String, Object>> mapList;
    private ImageView fotoDenuncia;
    private ProgressDialog load;

    @Override
    protected void onResume() {
        super.onResume();
        carregarDados();
    }

    @Override
    protected void onDestroy() {
        denunciaDAO.close();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        denunciaDAO = new DenunciaDAO(this);
        setContentView(R.layout.activity_listar);
        carregarDados();
    }

    public void carregarDados() {
        mapList = denunciaDAO.listarDenuncias();
        String[] chave = {
                DatabaseHelper.Denuncia.URI_MIDIA,
                DatabaseHelper.Denuncia.USUARIO,
                DatabaseHelper.Denuncia.CATEGORIA,
                DatabaseHelper.Denuncia.DESCRICAO,
                DatabaseHelper.Denuncia.DATA,
        };
        int[] valor = {R.id.foto, R.id.usuario, R.id.categoria, R.id.descricao, R.id.data};

        adapter = new SimpleAdapter(this,
                mapList,
                R.layout.layout_item_contato,
                chave,
                valor);
        listView = findViewById(R.id.listaDenuncias);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        fotoDenuncia = findViewById(R.id.foto);
        adapter.setViewBinder(this);
    }

    @Override
    public boolean setViewValue(View view, Object data, String textRepresentation) {
        if (view.getId() == R.id.foto) {
            try {
                if (data != null) {
                    String nomeArquivo = data.toString();
                    Uri imgUri = Uri.fromFile(new File(nomeArquivo));
                    Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imgUri));
                    fotoDenuncia.setImageBitmap(bitmap);
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    @Override
    public void onDialogEnviarParaNuvemClick(int id) {
        getPermissaoDaInternet();
        if (permisaoInternet) {
            Denuncia denuncia = denunciaDAO.buscarDenunciaPorId(id);
            UploadJson uploadJson = new UploadJson();
            uploadJson.execute(denuncia);
        }
    }

    @Override
    public void onDialogDetalheClick(int id) {
        Intent intent = new Intent(this, DetetalheActivity.class);
        intent.putExtra("id", id);
        startActivity(intent);
    }

    @Override
    public void onDialogExcluiClick(int id) {
        Denuncia denuncia = denunciaDAO.buscarDenunciaPorId(id);
        String path = denuncia.getUriMidia();
        if (denunciaDAO.removerDenuncia(id)) {
            File file = new File(path);
            file.delete();
            carregarDados();
        }
    }

    @Override
    public void onDialogEditarClick(int id) {
        Intent intent = new Intent(this, DenunciaFotoActivity.class);
        intent.putExtra("id", id);
        startActivity(intent);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        MenuDialogFragment fragmento = new MenuDialogFragment();
        Map<String, Object> item = mapList.get(position);
        Bundle bundle = new Bundle();
        bundle.putInt("id", (int) item.get(DatabaseHelper.Denuncia._ID));
        fragmento.setArguments(bundle);
        fragmento.show(this.getFragmentManager(), "confirma");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    if (isOnline()) {
                        permisaoInternet = true;
                        return;
                    } else {
                        permisaoInternet = false;
                        Toast.makeText(this, "Sem conexão de Internet.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    permisaoInternet = false;
                    Toast.makeText(this, "Sem permissão para uso de Internet.", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    private void getPermissaoDaInternet() {
        boolean internet = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED;
        boolean redeStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED;

        if (internet && redeStatus) {
            if (isOnline()) {
                permisaoInternet = true;
                return;
            } else {
                permisaoInternet = false;
                Toast.makeText(this, "Sem conexão de Internet.", Toast.LENGTH_LONG).show();
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.INTERNET,
                            Manifest.permission.ACCESS_NETWORK_STATE},
                    1);
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
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

    public void iniciarDownload(View view) {
        getPermissaoDaInternet();
        if (permisaoInternet) {
            DownloadDenuncias downloadDenuncias = new DownloadDenuncias();
            downloadDenuncias.execute();
        }
    }

    private class DownloadDenuncias extends AsyncTask<Long, Void, WebServiceUtils> {
        @Override
        protected void onPreExecute() {
            load = ProgressDialog.show(ListarActivity.this, "Por favor Aguarde ...", "Recuperando Informações do Servidor...");
        }

        @Override
        protected WebServiceUtils doInBackground(Long... ids) {
            WebServiceUtils webService = new WebServiceUtils();
            String id = (ids != null && ids.length == 1) ? ids[0].toString() : "";
            List<Denuncia> denuncias = webService.getListaDenunciasJson(url, "rest/denuncias", id);
            for (Denuncia denuncia : denuncias) {
                String path = getDiretorioDeSalvamento(denuncia.getUriMidia()).getPath();
                webService.downloadImagemBase64(url + "rest/arquivos", path, denuncia.getId());
                denuncia.setUriMidia(path);
            }
            return webService;
        }

        @Override
        protected void onPostExecute(WebServiceUtils webService) {
            for (Denuncia denuncia : webService.getDenuncias()) {
                Denuncia c = denunciaDAO.buscarDenunciaPorId(denuncia.getId());
                if (c != null) {
                    denunciaDAO.atualizarDenuncia(denuncia);
                } else {
                    denunciaDAO.inserirDenuncia(denuncia);
                }
            }
            load.dismiss();
            Toast.makeText(getApplicationContext(), webService.getRespostaServidor(), Toast.LENGTH_LONG).show();
            carregarDados();
        }
    }

    private class UploadJson extends AsyncTask<Denuncia, Void, WebServiceUtils> {
        @Override
        protected void onPreExecute() {
            load = ProgressDialog.show(ListarActivity.this, "Por favor Aguarde ...", "Recuperando Informações do Servidor...");
        }

        @Override
        protected WebServiceUtils doInBackground(Denuncia... denuncias) {
            WebServiceUtils webService = new WebServiceUtils();
            Denuncia denuncia = denuncias[0];
            String urlDados = url + "rest/denuncias";
            if (webService.sendDenunciaJson(urlDados, denuncia)){
                urlDados = url + "rest/arquivos";//postFotoBase64";
                webService.uploadImagemBase64(urlDados, getDiretorioDeSalvamento(denuncia.getUriMidia()));
            }
            return webService;
        }

        @Override
        protected void onPostExecute(WebServiceUtils webService) {
            Toast.makeText(getApplicationContext(),
                    webService.getRespostaServidor(),
                    Toast.LENGTH_LONG).show();
            load.dismiss();
        }
    }
}