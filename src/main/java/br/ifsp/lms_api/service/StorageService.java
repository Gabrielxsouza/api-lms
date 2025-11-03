package br.ifsp.lms_api.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Service
public class StorageService {

    // Define o caminho da pasta onde os uploads ficarão
    // "./uploads" significa uma pasta "uploads" na raiz do seu projeto
    private final Path rootLocation = Paths.get("uploads");

    public StorageService() {
        // Cria a pasta "uploads" se ela não existir
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível criar a pasta de upload", e);
        }
    }

    /**
     * Salva o arquivo e retorna a URL pública para acessá-lo.
     */
    public String salvarArquivo(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Falha ao salvar arquivo vazio.");
            }

            // Cria um nome de arquivo único para evitar conflitos
            // Ex: "meu-arquivo.pdf" -> "abcdef-12345-meu-arquivo.pdf"
            String originalFilename = file.getOriginalFilename();
            String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;

            // Resolve o caminho completo do arquivo
            Path destinationFile = this.rootLocation.resolve(Paths.get(uniqueFilename))
                                      .normalize().toAbsolutePath();

            // Copia o arquivo para a pasta de destino
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            // Retorna a URL pública para o arquivo
            // Ex: http://localhost:8080/uploads/abcdef-12345-meu-arquivo.pdf
            return ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/uploads/")
                .path(uniqueFilename)
                .toUriString();

        } catch (IOException e) {
            throw new RuntimeException("Falha ao salvar o arquivo.", e);
        }
    }
}