package com.example.simple_storage_app.controller;

import com.example.simple_storage_app.entity.StoredFile;
import com.example.simple_storage_app.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class StorageController {

    private final StorageService storageService;

    @GetMapping("/")
    public String index(Model model) {
        List<StoredFile> files = storageService.listAll();
        model.addAttribute("files", files);
        return "index";
    }

    @PostMapping("/upload")
    public String upload(@RequestParam("file") org.springframework.web.multipart.MultipartFile file,
                         RedirectAttributes redirectAttributes) {
        try {
            storageService.upload(file);
            redirectAttributes.addFlashAttribute("message", "File uploaded successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to upload: " + e.getMessage());
        }
        return "redirect:/";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            storageService.delete(id);
            redirectAttributes.addFlashAttribute("message", "File deleted.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSize(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "File is too large. Maximum size is 5 MB.");
        return "redirect:/";
    }
}
