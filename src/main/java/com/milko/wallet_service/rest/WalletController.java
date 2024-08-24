package com.milko.wallet_service.rest;

import com.milko.wallet_service.dto.input.ChangeWalletInputDto;
import com.milko.wallet_service.dto.input.WalletInputDto;
import com.milko.wallet_service.dto.input.WalletRequestInputDto;
import com.milko.wallet_service.dto.output.WalletOutputDto;
import com.milko.wallet_service.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/wallets")
public class WalletController {
    private final WalletService walletService;

    @PostMapping
    public WalletOutputDto create(@RequestBody WalletInputDto walletInputDto){
        log.info("IN WalletController create, walletInputDto = {}", walletInputDto);
        return walletService.create(walletInputDto);
    }

    @GetMapping
    public WalletOutputDto findById(@RequestBody WalletRequestInputDto walletRequestInputDto){
        return walletService.findById(walletRequestInputDto.getWalletId(), walletRequestInputDto.getProfileId());
    }

    @GetMapping("/{uuid}")
    public List<WalletOutputDto> findAllByProfileId(@PathVariable UUID uuid){
        return walletService.findAllByProfileId(uuid);
    }

    @PatchMapping
    public WalletOutputDto update(@RequestBody ChangeWalletInputDto changeWalletInputDto){
        return walletService.updateStatus(changeWalletInputDto);
    }

    @DeleteMapping
    public Boolean delete(@RequestBody WalletRequestInputDto walletRequestInputDto){
        return walletService.deleteById(walletRequestInputDto.getWalletId(), walletRequestInputDto.getProfileId());
    }
}
