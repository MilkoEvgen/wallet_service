package com.milko.wallet_service.rest;

import com.milko.wallet_service.dto.WalletTypeInputDto;
import com.milko.wallet_service.dto.WalletTypeOutputDto;
import com.milko.wallet_service.service.WalletTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/wallet_types")
public class WalletTypeController {
    private final WalletTypeService walletTypeService;

    @PostMapping
    public Integer create(@RequestBody WalletTypeInputDto walletTypeInputDto){
        return walletTypeService.create(walletTypeInputDto);
    }

    @GetMapping("/{id}")
    public WalletTypeOutputDto findById(@PathVariable Integer id){
        return walletTypeService.findById(id);
    }

    @GetMapping
    public List<WalletTypeOutputDto> findAll(){
        return walletTypeService.findAll();
    }

    @PatchMapping
    public WalletTypeOutputDto update(@RequestBody WalletTypeInputDto walletTypeInputDto){
        return walletTypeService.update(walletTypeInputDto);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable Integer id){
        return walletTypeService.deleteById(id);
    }
}
