package br.ufc.qx.qdetective;

import java.io.Serializable;
import java.util.Date;

public class Denuncia implements Serializable {

    public Integer id;
    public String descricao;
    public Date data;
    public String latitude;//Double
    public String longitude;//Double
    public String uriMidia;
    public String usuario;
    public String categoria;//ENUM

    public Denuncia() {
    }

    public Denuncia(Integer id, String descricao, Date data, String latitude, String longitude, String uriMidia, String usuario,String categoria) {
        this.id = id;
        this.descricao = descricao;
        this.data = data ;
        this.latitude = latitude;
        this.longitude = longitude;
        this.uriMidia = uriMidia;
        this.usuario = usuario;
        this.categoria = categoria;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getUriMidia() {
        return uriMidia;
    }

    public void setUriMidia(String uriMidia) {
        this.uriMidia = uriMidia;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Denuncia denuncia = (Denuncia) o;

        if (id != null ? !id.equals(denuncia.id) : denuncia.id != null) return false;
        if (descricao != null ? !descricao.equals(denuncia.descricao) : denuncia.descricao != null)
            return false;
        if (data != null ? !data.equals(denuncia.data) : denuncia.data != null) return false;
        if (latitude != null ? !latitude.equals(denuncia.latitude) : denuncia.latitude != null)
            return false;
        if (longitude != null ? !longitude.equals(denuncia.longitude) : denuncia.longitude != null)
            return false;
        if (uriMidia != null ? !uriMidia.equals(denuncia.uriMidia) : denuncia.uriMidia != null)
            return false;
        if (usuario != null ? !usuario.equals(denuncia.usuario) : denuncia.usuario != null)
            return false;
        return categoria != null ? categoria.equals(denuncia.categoria) : denuncia.categoria == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (descricao != null ? descricao.hashCode() : 0);
        result = 31 * result + (data != null ? data.hashCode() : 0);
        result = 31 * result + (latitude != null ? latitude.hashCode() : 0);
        result = 31 * result + (longitude != null ? longitude.hashCode() : 0);
        result = 31 * result + (uriMidia != null ? uriMidia.hashCode() : 0);
        result = 31 * result + (usuario != null ? usuario.hashCode() : 0);
        result = 31 * result + (categoria != null ? categoria.hashCode() : 0);
        return result;
    }
}
