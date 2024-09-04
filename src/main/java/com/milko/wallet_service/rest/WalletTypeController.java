package com.milko.wallet_service.rest;

import com.milko.wallet_service.dto.input.ChangeWalletTypeInputDto;
import com.milko.wallet_service.dto.input.WalletTypeInputDto;
import com.milko.wallet_service.dto.output.WalletTypeOutputDto;
import com.milko.wallet_service.service.WalletTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/wallet_types")
public class WalletTypeController {
    private final WalletTypeService walletTypeService;

    @PostMapping
    public UUID create(@RequestBody WalletTypeInputDto walletTypeInputDto){
        return walletTypeService.create(walletTypeInputDto);
    }

    @GetMapping("/{uuid}")
    public WalletTypeOutputDto findById(@PathVariable UUID uuid){
        return walletTypeService.findById(uuid);
    }

    @GetMapping
    public List<WalletTypeOutputDto> findAll(){
        return walletTypeService.findAll();
    }

    @PatchMapping
    public WalletTypeOutputDto update(@RequestBody ChangeWalletTypeInputDto changeWalletTypeInputDto){
        return walletTypeService.update(changeWalletTypeInputDto);
    }

    @DeleteMapping("/{uuid}")
    public Boolean delete(@PathVariable UUID uuid){
        return walletTypeService.deleteById(uuid);
    }
}
