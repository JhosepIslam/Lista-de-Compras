package com.example.lista_compra;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    EditText txtProducto;
    Button btnAgregar;
    ListView lvLista;
    ArrayList<Producto> lista;
    ArrayAdapter adaptador;
    private Object mActionMode;
    private Boolean flagUpdate = false;
    int Id_Producto = 0;
    Producto producto_gbl;
    ActionMode mode_gbl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();
        mostrarLista();

    }

    private void initialize() {
        txtProducto = (EditText)findViewById(R.id.txtCompra);
        btnAgregar = (Button)findViewById(R.id.btnIngresar);
        lvLista = (ListView)findViewById(R.id.lvLista);
    }

    public void AddOnList(View view){
        if (flagUpdate){
            if (txtProducto.getText().toString().isEmpty()){
                txtProducto.setError("Ingrese un producto!");
            }else{
                Base obj=new Base(this, "Productos",null,1);
                SQLiteDatabase objDb=obj.getWritableDatabase();
                String nuevoProducto= txtProducto.getText().toString();
                String sintaxisSql = "update Compras set Nombre = '"+nuevoProducto+"' where Id = "+producto_gbl.id+";";
                objDb.execSQL(sintaxisSql);
                txtProducto.setText("");
                txtProducto.requestFocus();
                btnAgregar.setText("Agregar");
                mode_gbl.finish();
                flagUpdate = false;
                mostrarLista();
                objDb.close();
            }
        } else {
            if (txtProducto.getText().toString().isEmpty()){
                txtProducto.setError("Ingrese un producto!");
            }else{
                Base obj=new Base(this, "Productos",null,1);
                SQLiteDatabase objDb=obj.getWritableDatabase();
                String nuevoProducto= txtProducto.getText().toString();
                String sintaxisSql = "insert into Compras(Id,Nombre)values("+null+",'"+ nuevoProducto + "')";
                objDb.execSQL(sintaxisSql);
                txtProducto.setText("");
                txtProducto.requestFocus();
                mostrarLista();
                objDb.close();
                View view_ = this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view_.getWindowToken(), 0);
                }
                lvLista.setSelection(lista.size()-1);
            }
        }

    }

    public void mostrarLista(){
        Base obj = new Base(this, "Productos", null, 1);
        SQLiteDatabase objDb = obj.getWritableDatabase();
        lista = new ArrayList<Producto>();
        Cursor cursor = objDb.rawQuery("select * from Compras",null);
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            Producto producto = new Producto(Integer.parseInt(cursor.getString(cursor.getColumnIndex("Id"))), cursor.getString(cursor.getColumnIndex("Nombre")));
            lista.add(producto);
            cursor.moveToNext();
        }

        adaptador = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, lista);
        lvLista.setAdapter(adaptador);
        objDb.close();

        lvLista.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Producto producto = (Producto) adaptador.getItem(position);
                Id_Producto = producto.id;
                producto_gbl = producto;
                mActionMode = MainActivity.this.startActionMode(amc);
                view.setSelected(true);
                return true;
            }
        });
    }

    private ActionMode.Callback amc = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            getMenuInflater().inflate(R.menu.options, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if(item.getItemId() == R.id.eliminarId) {
                eliminarProducto();
                mode.finish();
            } else {
                flagUpdate = true;
                btnAgregar.setText("Actualizar");
                txtProducto.setText(producto_gbl.nombre);
                mode_gbl = mode;
            }

            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            flagUpdate = false;
            btnAgregar.setText("Agregar");
            txtProducto.setText("");
        }
    };

    public void borrarLista(){
        Base obj=new Base(this, "Productos",null,1);
        SQLiteDatabase objDb=obj.getWritableDatabase();
        Cursor cursor=objDb.rawQuery("SELECT * FROM Compras", null);
        if(cursor.moveToNext()) {
            objDb.delete("Compras", null, null);
            lista.clear();
            //Notifica que los datos se han modificado,cualquier Vista que refleje el conjunto
            // de datos debe actualizarse.
            adaptador.notifyDataSetChanged();
            objDb.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = 'Compras'");
            objDb.close();
        }
    }

    public void eliminarProducto() {
        Base obj = new Base(this, "Productos", null, 1);
        SQLiteDatabase objDb = obj.getReadableDatabase();
        int id = Id_Producto;
        objDb.execSQL("DELETE FROM Compras WHERE Id="+id+";");
        mostrarLista();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog dialog;
        switch (id){
            case R.id.borrarLista:
                builder.setTitle("Borrar lista");
                builder.setMessage("¿Esta seguro que desea eiminar definitivamente la lista?");
                builder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        borrarLista();
                    }
                });
                builder.setNegativeButton("NO", null);
                dialog = builder.create();
                dialog.show();
                break;
            case R.id.about:
                builder.setTitle("Acerca de:");
                builder.setMessage("Creado por: Jhosep Islam");
                builder.setPositiveButton("Aceptar", null);
                dialog = builder.create();
                dialog.show();
                break;
            default:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}