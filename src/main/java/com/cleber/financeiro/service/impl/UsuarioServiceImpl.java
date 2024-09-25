package com.cleber.financeiro.service.impl;

import com.cleber.financeiro.exception.ErroDeAutenticacao;
import com.cleber.financeiro.exception.RegraDeNegocioException;
import com.cleber.financeiro.model.entity.Usuario;
import com.cleber.financeiro.model.repository.UsuarioRepository;
import com.cleber.financeiro.service.UsuarioService;
import jakarta.transaction.Transactional;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
@NoArgsConstructor
@Service
public class UsuarioServiceImpl implements UsuarioService {
    @Autowired
    UsuarioRepository usuarioRepository;
    
    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        super();
        this.usuarioRepository = usuarioRepository;
    }
    
    @Override
    public Usuario autenticarUsuario(String email, String senha) {

        Optional<Usuario> usuario = usuarioRepository.findByEmail(email);
        if (!usuario.isPresent()){
            throw new ErroDeAutenticacao("Usuario não encontrado pelo email informado");
        }
        if (!usuario.get().getSenha().equals(senha)){
            throw new ErroDeAutenticacao("Senha inválida");
        }
        return usuario.get();
    }
    
    @Override
    @Transactional
    public Usuario salvarUsuario(Usuario usuario) {

        validarEmail(usuario.getEmail());
        return usuarioRepository.save(usuario);
    }
    
    @Override
    public void validarEmail(String email) {

        boolean verificarSeOEmailExisteNaBaseDeDados = usuarioRepository.existsByEmail(email);
        if (verificarSeOEmailExisteNaBaseDeDados){
            throw new RegraDeNegocioException("Ja existe um usuario com esse email.");
        }
    
    }
    
    @Override
    public Optional<Usuario> obterUsuarioPorId(Long id) {
        return usuarioRepository.findById(id);
    }
}
