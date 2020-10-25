package com.davi.whatsapp.activity;

import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.davi.whatsapp.config.ConfiguracaoFirebase;
import com.davi.whatsapp.helper.Base64Custom;
import com.davi.whatsapp.helper.UsuarioFirebase;
import com.davi.whatsapp.model.Mensagem;
import com.davi.whatsapp.model.Usuario;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.davi.whatsapp.R;
import com.google.firebase.database.DatabaseReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private TextView textViewNome;
    private CircleImageView circleImageViewFoto;
    private Usuario usuarioDestinatario;
    private EditText editMensagem;

    //identificador usuarios remetente e destinatário
    private String idUsuarioRemetente;
    private String idUsuarioDestinatario;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //Configurar toobar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Configurações iniciais
        textViewNome = findViewById(R.id.textViewNomeChat);
        circleImageViewFoto =findViewById(R.id.circleImageFotoChat);
        editMensagem = findViewById(R.id.editMensagem);

        //Recupera dados do usuário remetente
        idUsuarioRemetente = UsuarioFirebase.getIdentificadorUsuario();

        //Recuperar dados do usuário destinatário
        Bundle bundle = getIntent().getExtras();
        if(bundle !=null){
            usuarioDestinatario = (Usuario)bundle.getSerializable("chatContato");
            textViewNome.setText(usuarioDestinatario.getNome());
            String foto = usuarioDestinatario.getFoto();
            if(foto!=null){
                Uri url = Uri.parse(usuarioDestinatario.getFoto());
                Glide.with(ChatActivity.this)
                        .load(url)
                        .into(circleImageViewFoto);
            }else{
                circleImageViewFoto.setImageResource(R.drawable.padrao);
            }
            //Recuperar dados usuario destinatario
            idUsuarioDestinatario = Base64Custom.codificarBase64(usuarioDestinatario.getEmail());
        }


    }

    public void enviarMensagem(View view){
        String textoMensagem = editMensagem.getText().toString();
        if(!textoMensagem.isEmpty()){
            Mensagem mensagem = new Mensagem();
            mensagem.setIdUsuario(idUsuarioRemetente);
            mensagem.setMensagem(textoMensagem);

            //Salvar Mensagem
            salvarMensagem(idUsuarioRemetente,idUsuarioDestinatario,mensagem);

        }else{
            Toast.makeText(ChatActivity.this, "Digite uma mensagem para enviar!", Toast.LENGTH_LONG).show();
        }
    }

    private void salvarMensagem(String idRemetente,String idDestinatario, Mensagem msg){
        DatabaseReference database = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference mensagemRef = database.child("mensagens");
        mensagemRef.child(idRemetente)
                .child(idDestinatario)
                .push()
                .setValue(msg);

        //Limpar texto
        editMensagem.setText("");

    }
}