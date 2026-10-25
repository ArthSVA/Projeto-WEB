package br.senac.tads.dsw.dados_pessoais;

import java.util.List;
import java.util.Optional;
import java.net.URI;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/pessoas")
public class PessoaController {

    private final PessoaService PessoaService;

    public PessoaController(PessoaService PessoaService) {
        this.PessoaService = PessoaService;
    }

    @GetMapping
    public List<PessoaDto> obterPessoas() {
        return PessoaService.obterPessoas();
    }

    @GetMapping("/busca")
    public ResponseEntity<List<PessoaDto>> buscarPessoas(@RequestParam("q") String termo) {
        return ResponseEntity.ok(PessoaService.buscarPessoas(termo));
    }   

    @GetMapping("/{username}")
    public PessoaDto obterPessoa(@PathVariable("username") String username) {
        Optional<PessoaDto> optPessoa = PessoaService.obterPessoa(username);
        if (optPessoa.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return optPessoa.get();
    }

    @PostMapping("/sem-validacao")
    public ResponseEntity<?> incluirNovo(@RequestBody PessoaDto pessoa) {
        PessoaService.incluirNovaPessoa(pessoa);
        URI location = ServletUriComponentsBuilder
            .fromCurrentContextPath()
            .path("/pessoas/{username}")
            .buildAndExpand(pessoa.getUsername())
            .toUri();
        return ResponseEntity.created(location).build();
    }

    @PostMapping
    public ResponseEntity<?> incluirNovoComValidacao(@RequestBody @Valid PessoaDto pessoa) {
        // NOTAR O @Valid na linha acima

        PessoaService.incluirNovaPessoa(pessoa);
        URI location = ServletUriComponentsBuilder
            .fromCurrentContextPath()
            .path("/pessoas/{username}")
            .buildAndExpand(pessoa.getUsername())
            .toUri();
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/{username}")
    public ResponseEntity<?> atualizar(@PathVariable("username") String username,
            @RequestBody @Valid PessoaAlteracaoDto pessoa) {
        PessoaDto pessoaAlterada = PessoaService.alterarPessoa(username, pessoa);
        return ResponseEntity.ok().body(pessoaAlterada);
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<?> remover(@PathVariable("username") String username) {
        PessoaService.removerPessoa(username);
        return ResponseEntity.noContent().build(); // HTTP 204
    }

    @ExceptionHandler(NaoEncontradoException.class)
    public ResponseEntity<ProblemDetail> tratarExcecao(NaoEncontradoException ex) {
    // ProblemDetail - padrão RFC 7807 para representar erros HTTP
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
            HttpStatusCode.valueOf(404), ex.getMessage());
        return ResponseEntity.of(pd).build();
    }
}