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

    private final Path rootLocation = Paths.get("uploads");

    public StorageService() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível criar a pasta de upload", e);
        }
    }

    public String createArquivo(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Falha ao salvar arquivo vazio.");
            }

            String originalFilename = file.getOriginalFilename();
            String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;

            Path destinationFile = this.rootLocation.resolve(Paths.get(uniqueFilename))
                                      .normalize().toAbsolutePath();

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            return ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/uploads/")
                .path(uniqueFilename)
                .toUriString();

        } catch (IOException e) {
            throw new RuntimeException("Falha ao salvar o arquivo.", e);
        }
    }
public void deleteFile(String filename) throws IOException { // Este método pode lançar IOException
        if (filename == null || filename.isEmpty()) {
            return; 
        }

        Path fileToDelete = this.rootLocation.resolve(Paths.get(filename))
                                .normalize().toAbsolutePath();

        // --- MUDANÇA ---
        // Trocado de deleteIfExists para delete.
        // Agora, ele lançará NoSuchFileException se o arquivo não for encontrado.
        Files.delete(fileToDelete); 
    }
}