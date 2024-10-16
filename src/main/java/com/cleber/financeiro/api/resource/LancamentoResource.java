package com.cleber.financeiro.api.resource;

import com.cleber.financeiro.api.converter.ConvertDtoToEntity;
import com.cleber.financeiro.api.dto.AtualizarStatusDTO;
import com.cleber.financeiro.api.dto.LancamentoDTO;
import com.cleber.financeiro.exception.RegraDeNegocioException;
import com.cleber.financeiro.model.entity.Lancamento;
import com.cleber.financeiro.model.entity.StatusLancamento;
import com.cleber.financeiro.model.entity.Usuario;
import com.cleber.financeiro.service.LancamentoService;
import com.cleber.financeiro.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/lancamentos")
@RequiredArgsConstructor
public class LancamentoResource {
    
    private LancamentoService lancamentoService;
    private UsuarioService usuarioService;
    private final ConvertDtoToEntity toEntidadeLancamento;
    
    @PostMapping
    public ResponseEntity salvarLancamento(@RequestBody LancamentoDTO dto) {
        try {
            Lancamento converteEntidade = toEntidadeLancamento.converterDtoParaEntidade(dto);
            converteEntidade = lancamentoService.salvarLancamento(converteEntidade);
            return new ResponseEntity(converteEntidade, HttpStatus.CREATED);
            
        } catch (RegraDeNegocioException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PutMapping("{id}")
    public ResponseEntity atualizarLancamento(@PathVariable("id") Long id, @RequestBody LancamentoDTO dto) {
        
        return lancamentoService.obterLancamentoPorId(id).map(entity -> {
            try {
                Lancamento lancamento = toEntidadeLancamento.converterDtoParaEntidade(dto);
                lancamento.setId(entity.getId());
                lancamentoService.atualizarLancamento(lancamento);
                return ResponseEntity.ok(lancamento);
            } catch (RegraDeNegocioException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }).orElseGet(() -> new ResponseEntity("Lancamento não encontrado", HttpStatus.BAD_REQUEST));
    }
    
    @PutMapping("{id}/atualizar-status")
    public ResponseEntity atualizarStatus(@PathVariable("id") Long id, @RequestBody AtualizarStatusDTO dto) {
        return lancamentoService.obterLancamentoPorId(id).map(entity -> {
            StatusLancamento selecionarStatus = StatusLancamento.valueOf(dto.getStatus());
            
            if (lancamentoService == null) {
                return ResponseEntity.badRequest().body("O status informado não existe " + "[" + dto + "]" + " informar um status válido");
            }
            try {
                entity.setStatusLancamento(selecionarStatus);
                lancamentoService.atualizarLancamento(entity);
                return ResponseEntity.ok(entity);
            } catch (RegraDeNegocioException status) {
                return ResponseEntity.badRequest().body(status.getMessage());
            }
        }).orElseGet(() -> new ResponseEntity("Status não encontrado ", HttpStatus.BAD_REQUEST));
    }
    
    @GetMapping
    public ResponseEntity buscar(
            @RequestParam(value = "descricao", required = false) String descricao,
            @RequestParam(value = "mes", required = false) Integer mes,
            @RequestParam(value = "ano", required = false) Integer ano,
            @RequestParam(value = "usuario", required = false) Long idusuario
    ) {
        if (idusuario == null) {
            return ResponseEntity.badRequest().body("O ID do usuário é obrigatório " + "[" + idusuario + "]");
        }
        Lancamento lancamentoFiltro = new Lancamento();
        lancamentoFiltro.setDescricao(descricao);
        lancamentoFiltro.setMes(mes);
        lancamentoFiltro.setAno(ano);
        
        Optional<Usuario> usuario = usuarioService.obterUsuarioPorId(idusuario);
        
        if (usuario.isPresent()) {
            return ResponseEntity.badRequest().body("Consulta não realizada, o usuario não existe");
        } else {
            lancamentoFiltro.setUsuario(usuario.get());
        }
        
        List<Lancamento> lancamentos = lancamentoService.buscarLancamento(lancamentoFiltro);
        return ResponseEntity.ok(lancamentos);
    }
    
    @DeleteMapping("{id}")
    public ResponseEntity deletar(@PathVariable("id") Long id) {
        return lancamentoService.obterLancamentoPorId(id).map(entity -> {
            lancamentoService.deletarLancamento(entity);
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        }).orElseGet(() ->
                new ResponseEntity("Lançamento não encontrado na base de dados", HttpStatus.BAD_REQUEST));
    }
}
