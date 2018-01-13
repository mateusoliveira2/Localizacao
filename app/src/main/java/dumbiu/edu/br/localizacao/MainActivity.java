package dumbiu.edu.br.localizacao;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TextView txtLatitude;
    private TextView txtLongitude;
    private TextView txtCidade;
    private TextView txtEstado;
    private TextView txtPais;
    private TextView txtDistancia;

    String towers;
    double latitude ;
    double longitude;
    double lat2 = -6.938262;
    double long2 = -35.791749;

    private Location location;
    private LocationManager locationManager;

    private Address endereco;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtDistancia = (TextView) findViewById(R.id.txtDistancia);
        txtLatitude = (TextView) findViewById(R.id.txtLat);
        txtLongitude= (TextView) findViewById(R.id.txtLong);
        txtCidade = (TextView) findViewById(R.id.txtCidade);
        txtEstado = (TextView) findViewById(R.id.txtEstado);
        txtPais = (TextView) findViewById(R.id.txtPais);

        //VERIFICAÇÃO DE AUTORIZAÇÃO PARA USAR LOCALIZAÇÃO E TALS
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    2);

            Toast.makeText(this, "SEM PERMISSÃO", Toast.LENGTH_SHORT).show();

        }else{
            //Toast.makeText(this, "TUDO TOP", Toast.LENGTH_SHORT).show();
            locationManager = (LocationManager)
                    getSystemService(Context.LOCATION_SERVICE);

            Criteria crit = new Criteria();
            towers = locationManager.getBestProvider(crit, false);
            location = getLastKnownLocation();
        }

        if (location != null) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();

            txtLongitude.setText("Longitude: " + longitude);
            txtLatitude.setText("Latitude: " + latitude);

            try{
                endereco = buscarEndereco(latitude, longitude);
                txtCidade.setText("Cidade: " + endereco.getSubLocality());
                txtEstado.setText("Estado: " +endereco.getAdminArea());
                txtPais.setText("País: "+endereco.getCountryName());
                txtDistancia.setText("Distancia: " + distance(latitude, lat2,longitude,long2,0.0,0.0));

            }catch (IOException e){
                Log.i("DEU PAU NO ENDEREÇO",e.getMessage());
            }

        }else{
            Toast.makeText(this, "Location is null! " + towers, Toast.LENGTH_SHORT).show();

        }
    }

    public Address buscarEndereco(double latitude, double longitude) throws IOException{
        Geocoder geocoder;
        Address address = null;
        List<Address> addresses;

        geocoder = new Geocoder(getApplicationContext());
        //PASSANDO A LATITUDE E LONGITUDE QUE TEMOS, E QUERENDO APENAS 1 RESULTADO
        addresses = geocoder.getFromLocation(latitude, longitude,1);
        if (addresses.size() > 0)
            address = addresses.get(0);

        return address;
    }
    //ISSO AQUI VERIFICA A ULTIMA LOCALIZAÇÃO, MAS ANTES VERIFICA TODOS OS PROVEDORES, PQ SO PELO GPS TAVA DANDO ERRO AQUI
    private Location getLastKnownLocation() {
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        Location l = null;
        for (String provider : providers) {
            try {
                l = locationManager.getLastKnownLocation(provider);
            } catch (SecurityException e) {
                //Toast.makeText(this, "sei la " + towers, Toast.LENGTH_SHORT).show();
            }

            if (l == null) {
                continue;
            }
            if (bestLocation == null
                    || l.getAccuracy() < bestLocation.getAccuracy()) {
                Log.d("found best location: %s", String.valueOf(1));
                bestLocation = l;
            }
        }
        if (bestLocation == null) {
            return null;
        }
        return bestLocation;
    }

    private double distance(double lat1, double lat2, double lon1, double lon2,
                            double el1, double el2) {

        final int R = 6371; // Radius of the earth

        Double latDistance = deg2rad(lat2 - lat1);
        Double lonDistance = deg2rad(lon2 - lon1);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;
        distance = Math.pow(distance, 2) + Math.pow(height, 2);
        return Math.sqrt(distance);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
}
