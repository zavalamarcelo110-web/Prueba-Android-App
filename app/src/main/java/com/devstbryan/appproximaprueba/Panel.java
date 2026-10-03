package com.devstbryan.appproximaprueba;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.EventListener;

public class Panel extends AppCompatActivity {


    // Crear Variables

    private Button btnLinterna;

    private Button btnSegundaVentana;

    private Button btnUbicacion;

    private Button btnMap;

    TextView tvUbicacion;


    // Variables para Linterna

    CameraManager cameraManager;

    String idCamara;
    boolean LinternaEncendida = false;

    // Variable para Ubicacion

    double latitud = 0;

    double longitud = 0;

    boolean ubicacionObtenida = false;

    // Variables para Codigo de los Permisos

    final int PERMISO_UBICACION = 100;

    final int PERMISO_CAMARA = 200;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_panel);

        // Conexion de Componentes del XML
        btnLinterna = findViewById(R.id.btnLinterna);
        btnSegundaVentana = findViewById(R.id.btnSegundaVentana);
        btnUbicacion = findViewById(R.id.btnUbicacion);
        btnMap = findViewById(R.id.btnMap);
        tvUbicacion = findViewById(R.id.tvUbicacion);

        // Evento Ventana

        btnSegundaVentana.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        // INTENT EXPLICITO
                        Intent segunda_vista = new Intent(
                                Panel.this, Segunda_Vista.class
                        );
                        startActivity(segunda_vista);
                    }
                }
        );

        //Preparacion para Linterna

        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        // Utilizamos TRY-CATCH

        try {
            String[] camara =
                    cameraManager.getCameraIdList();
            if(camara.length > 0){
                idCamara = camara[0];
            }
        } catch (CameraAccessException e) {
            Toast.makeText(this, "Error al Acceder a la Camara", Toast.LENGTH_SHORT).show();
        }

        // Crear BTN de Evento Linterna

        btnLinterna.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Validamos el permiso de la Linterna
                        if(ActivityCompat.checkSelfPermission(
                                Panel.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                            // Solicitamos el Permiso
                            ActivityCompat.requestPermissions(
                                    Panel.this,
                                    new  String[]{
                                            Manifest.permission.CAMERA
                                    },
                                    // Indica el estado del codigo 200
                                    PERMISO_CAMARA
                            );
                            return;
                        }
                    }
                }
        );


    }
}