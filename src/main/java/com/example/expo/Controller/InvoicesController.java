package com.example.expo.Controller;

import com.example.expo.Api.ApiResponse;
import com.example.expo.DTO.InvoiceRequest;
import com.example.expo.Model.Invoices;
import com.example.expo.Service.InvoicesService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/invoices")
public class InvoicesController {
    private final InvoicesService invoicesService;


    @GetMapping("/get")
    public ResponseEntity<?> getAllInvoices() {
        List<Invoices> invoicesList = invoicesService.getAll();
        return ResponseEntity.status(200).body(invoicesList);
    }

    @PostMapping("/add/{merchantId}")
    public ResponseEntity<?> addInvoice(@PathVariable Integer merchantId, @RequestBody @Valid InvoiceRequest invoice) {
        invoicesService.addInvoice(merchantId, invoice);
        return ResponseEntity.status(200).body(new ApiResponse("added done"));
    }

    @PutMapping("/update/{invoiceId}")
    public ResponseEntity<?> updateInvoice(@PathVariable Integer invoiceId, @RequestBody @Valid InvoiceRequest invoice
    ) {
        invoicesService.updateInvoice(invoiceId, invoice);
        return ResponseEntity.status(200).body(new ApiResponse("updated done"));
    }

    @DeleteMapping("/delete/{invoiceId}")
    public ResponseEntity<?> deleteInvoice(@PathVariable Integer invoiceId) {
        invoicesService.deleteInvoice(invoiceId);
        return ResponseEntity.status(200).body(new ApiResponse("deleted done"));
    }


}