package com.example.expo.Service;
import com.example.expo.DTO.InvoiceResponse;

import com.example.expo.Api.ApiException;
import com.example.expo.DTO.InvoiceRequest;
import com.example.expo.Model.Invoices;
import com.example.expo.Model.Sellerers;
import com.example.expo.Repository.InvoicesRepository;
import com.example.expo.Repository.SellersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoicesService {
    private final InvoicesRepository invoicesRepository;
    private final SellersRepository sellersRepository;




    // صيغة عرض موحدة مع مسار الفاتورة الواحدة؛ لا تتغير حسابات أو حالات الفواتير.
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<InvoiceResponse> getAll() {
        return invoicesRepository.findAll().stream().map(InvoiceResponse::from).toList();
    }
    public void addInvoice(Integer sellerId, InvoiceRequest request) {
        Sellerers sellerers = sellersRepository.findSellerById(sellerId);

        if (sellerers == null) {
            throw new ApiException("seller id not found");
        }


        Invoices invoice = new Invoices();

        invoice.setSeller(sellerers);//الفاتورة مرتبط بالتاجر
        invoice.setInvoice_number(request.getInvoice_number());
//        invoice.setUser_id(request.getUser_id());
        invoice.setDescription(request.getDescription());
        invoice.setSubtotal(request.getSubtotal());
        invoice.setTax_amount(request.getTax_amount());
        // الإجمالي يحسبه النظام.
        invoice.setTotal_amount(
                request.getSubtotal().add(request.getTax_amount())
        );

        invoice.setQr_token(request.getQr_token());
        invoice.setExpires_at(request.getExpires_at());

        invoice.setStatus("PENDING");
        invoice.setPaid_at(null);

        LocalDateTime now = LocalDateTime.now();
        invoice.setCreated_at(now);
//        invoice.setUpdated_at(now);

        invoicesRepository.save(invoice);
    }

//    public void addInvoice(Integer sellerId, Invoices invoice) {
//        Sellerers seller = sellersRepository.findSellerById(sellerId);
//
//        if (seller == null) {
//            throw new ApiException("seller id not found");
//        }
//        // ما ننشئ فاتورة الا تكون مرتبط بتاجر
//        invoice.setSeller(seller);
//        invoice.setCreated_at(LocalDateTime.now());// وقت ألإنشاء عند ألإضافة
//
//        invoicesRepository.save(invoice);
//    }



//    public void updateInvoice(Integer invoiceId, Invoices invoice) {
//        Invoices oldInvoice =
//                invoicesRepository.findInvoiceById(invoiceId);
//
//        if (oldInvoice == null) {
//            throw new ApiException("invoice id not found");
//        }
//       // مافي اي تعديل على اي اي دي
//        oldInvoice.setInvoice_number(invoice.getInvoice_number());
//        oldInvoice.setDescription(invoice.getDescription());
//        oldInvoice.setSubtotal(invoice.getSubtotal());
//        oldInvoice.setTax_amount(invoice.getTax_amount());
//        oldInvoice.setTotal_amount(invoice.getTotal_amount());
//        oldInvoice.setStatus(invoice.getStatus());
//        oldInvoice.setQr_token(invoice.getQr_token());
//        oldInvoice.setExpires_at(invoice.getExpires_at());
//        oldInvoice.setPaid_at(invoice.getPaid_at());
//        oldInvoice.setUpdated_at(LocalDateTime.now());//التعديل بالتاريخ الحالي
//        invoice.setSeller(invoice.getSeller());
//        invoicesRepository.save(oldInvoice);
//    }


    public void updateInvoice(Integer invoiceId, InvoiceRequest request) {
        Invoices oldInvoice =
                invoicesRepository.findInvoiceById(invoiceId);

        if (oldInvoice == null) {
            throw new ApiException("invoice id not found");
        }

        // لا نغيّر تفاصيل فاتورة مدفوعة أو ملغاة أو منتهية.
        if (!"PENDING".equals(oldInvoice.getStatus())) {
            throw new ApiException("only pending invoices can be updated");
        }

        if (oldInvoice.getExpires_at() != null
                && !oldInvoice.getExpires_at().isAfter(LocalDateTime.now())) {
            throw new ApiException("expired invoices cannot be updated");
        }

        oldInvoice.setInvoice_number(request.getInvoice_number());
        oldInvoice.setDescription(request.getDescription());
        oldInvoice.setSubtotal(request.getSubtotal());
        oldInvoice.setTax_amount(request.getTax_amount());

        oldInvoice.setTotal_amount(request.getSubtotal().add(request.getTax_amount()));
        //لان البيج ديسيمل هو كائن ما ينفع معه الجمع والطرح لذلك استخدمنا add

        oldInvoice.setQr_token(request.getQr_token());
        oldInvoice.setExpires_at(request.getExpires_at());
//        oldInvoice.setUpdated_at(LocalDateTime.now());

        // نحافظ على seller وuser_id وcreated_at وstatus وpaid_at.
        invoicesRepository.save(oldInvoice);
    }


    public void deleteInvoice(Integer invoiceId) {
        Invoices oldInvoice =
                invoicesRepository.findInvoiceById(invoiceId);

        if (oldInvoice == null) {
            throw new ApiException("invoice id not found");
        }

        invoicesRepository.delete(oldInvoice);
    }



    // عرض فاتورة واحدة بنفس DTO المستخدم في قائمة الفواتير.
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public InvoiceResponse getById(Integer invoiceId) {
        Invoices invoice = invoicesRepository.findById(invoiceId)
                .orElseThrow(() -> new ApiException("invoice id not found"));
        return InvoiceResponse.from(invoice);
    }

}