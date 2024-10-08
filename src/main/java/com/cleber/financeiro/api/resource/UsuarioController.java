package com.cleber.financeiro.api.resource;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.cleber.financeiro.api.dto.UsuarioAutenticacaoDTO;

import com.cleber.financeiro.api.dto.UsuarioCadastroDTO;
import com.cleber.financeiro.exception.ErroDeAutenticacao;
import com.cleber.financeiro.exception.RegraDeNegocioException;
import com.cleber.financeiro.model.entity.Usuario;
import com.cleber.financeiro.service.UsuarioService;

@RestController
@RequestMapping(path = "/api/usuarios")
public class UsuarioController {

    public UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/autenticar")
    public ResponseEntity autenticarUsuario(@RequestBody UsuarioAutenticacaoDTO dtoAuth) {
        try {
            Usuario usuarioAutenticado = usuarioService.autenticarUsuario(dtoAuth.getEmail(), dtoAuth.getSenha());
            return ResponseEntity.ok(usuarioAutenticado);
        } catch (ErroDeAutenticacao e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity salvar(@RequestBody UsuarioCadastroDTO dto) {
        Usuario usuario = Usuario.builder()
                .nomeCompleto(dto.getNomeCompleto())
                .cpf(dto.getCpf())
                .nomeUsuario(dto.getNomeUsuario())
                .email(dto.getEmail())
                .senha(dto.getSenha())
                /*nigume vai passar a data de cadastro em sistema nenhum*/
                .dataCadastro(dto.getDataCadastro())
                .build();

        try {
            Usuario usuarioSalvo = usuarioService.salvarUsuario(usuario);
            return new ResponseEntity(usuarioSalvo, HttpStatus.CREATED);
        } catch (RegraDeNegocioException regra) {
            return ResponseEntity.badRequest().body(regra.getMessage());
        }
    }
}
