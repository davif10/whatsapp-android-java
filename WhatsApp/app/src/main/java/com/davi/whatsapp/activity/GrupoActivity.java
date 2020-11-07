package com.davi.whatsapp.activity;

import android.content.Intent;
import android.os.Bundle;

import com.davi.whatsapp.adapter.ContatosAdapter;
import com.davi.whatsapp.adapter.GrupoSelecionadoAdapter;
import com.davi.whatsapp.config.ConfiguracaoFirebase;
import com.davi.whatsapp.helper.RecyclerItemClickListener;
import com.davi.whatsapp.helper.UsuarioFirebase;
import com.davi.whatsapp.model.Usuario;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.AdapterView;

import com.davi.whatsapp.R;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GrupoActivity extends AppCompatActivity {
    private RecyclerView recyclerMembrosSelecionados, recyclerMembros;
    private ContatosAdapter contatosAdapter;
    private GrupoSelecionadoAdapter grupoSelecionadoAdapter;
    private List<Usuario> listaMembros = new ArrayList<>();
    private List<Usuario> listaMembrosSelecionados = new ArrayList<>();
    private ValueEventListener valueEventListenerMembros;
    private DatabaseReference usuarioRef;
    private FirebaseUser usuarioAtual;
    private Toolbar toolbar;
    private FloatingActionButton fabAvancarCadastro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grupo);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Novo Grupo");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Confugurações Iniciais
        recyclerMembrosSelecionados = findViewById(R.id.recyclerMembrosSelecionados);
        recyclerMembros = findViewById(R.id.recyclerMembros);
        usuarioRef = ConfiguracaoFirebase.getFirebaseDatabase().child("usuarios");
        usuarioAtual = UsuarioFirebase.getUsuarioAtual();
        fabAvancarCadastro = findViewById(R.id.fabAvancarCadastro);

        //Configurando adapter
        contatosAdapter = new ContatosAdapter(listaMembros,getApplicationContext());

        //Configurando RecyclerView para os contatos
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerMembros.setLayoutManager(layoutManager);
        recyclerMembros.setHasFixedSize(true);
        recyclerMembros.setAdapter(contatosAdapter);

        recyclerMembros.addOnItemTouchListener(new RecyclerItemClickListener(
                getApplicationContext(),
                recyclerMembros,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Usuario usuarioSelecionado  = listaMembros.get(position);

                        //Remover Usuario selecionado da lista
                        listaMembros.remove(usuarioSelecionado);
                        contatosAdapter.notifyDataSetChanged();
                        //Adiciona usuario na lista de usuários selecionados
                        listaMembrosSelecionados.add(usuarioSelecionado);
                        grupoSelecionadoAdapter.notifyDataSetChanged();
                        atualizarMembrosToolbar();

                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    }
                }
        ));

        //Configurando RecyclerView para os membros selecionados

        grupoSelecionadoAdapter = new GrupoSelecionadoAdapter(listaMembrosSelecionados,getApplicationContext());
        RecyclerView.LayoutManager layoutManagerHorizontal = new LinearLayoutManager(
                getApplicationContext(),
                LinearLayoutManager.HORIZONTAL,
                false
        );
        recyclerMembrosSelecionados.setLayoutManager(layoutManagerHorizontal);
        recyclerMembrosSelecionados.setHasFixedSize(true);
        recyclerMembrosSelecionados.setAdapter(grupoSelecionadoAdapter);

        recyclerMembrosSelecionados.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getApplicationContext(),
                        recyclerMembrosSelecionados,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                Usuario usuarioSelecionado = listaMembrosSelecionados.get(position);

                                //Remover da listagem de membros selecionados
                                listaMembrosSelecionados.remove(usuarioSelecionado);
                                grupoSelecionadoAdapter.notifyDataSetChanged();

                                //Adicionar a listagem de membros
                                listaMembros.add(usuarioSelecionado);
                                contatosAdapter.notifyDataSetChanged();

                                atualizarMembrosToolbar();
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        }
                )
        );

        //Configurar floating action button
        fabAvancarCadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(GrupoActivity.this,CadastroGrupoActivity.class);
                i.putExtra("membros",(Serializable) listaMembrosSelecionados);
                startActivity(i);
            }
        });
    }//Fim onCreate

    public void atualizarMembrosToolbar(){
        int totalSelecionados = listaMembrosSelecionados.size();
        int total = listaMembros.size() + totalSelecionados;
        toolbar.setSubtitle(totalSelecionados+" de "+ total + " selecionados.");
    }

    public void recuperarContatos(){
        valueEventListenerMembros = usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dados: dataSnapshot.getChildren()){

                    Usuario usuario = dados.getValue(Usuario.class);
                    String emailUsuarioAtual = usuarioAtual.getEmail();
                    if(!emailUsuarioAtual.equals(usuario.getEmail())){
                        listaMembros.add(usuario);
                    }

                }
                contatosAdapter.notifyDataSetChanged();
                atualizarMembrosToolbar();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarContatos();
    }

    @Override
    public void onStop() {
        super.onStop();
        usuarioRef.removeEventListener(valueEventListenerMembros);
    }
}