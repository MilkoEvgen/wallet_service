package com.milko.wallet_service.rest;

import com.milko.wallet_service.dto.output.WalletTypeStatusHistoryOutputDto;
import com.milko.wallet_service.mapper.WalletTypeStatusHistoryMapper;
import com.milko.wallet_service.service.WalletTypeStatusHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/wallet_types_history")
@RequiredArgsConstructor
public class WalletTypeStatusHistoryController {
    private final WalletTypeStatusHistoryService walletTypeStatusHistoryService;
    private final WalletTypeStatusHistoryMapper mapper;

    @GetMapping("{uuid}")
    List<WalletTypeStatusHistoryOutputDto> findAllByWalletTypeId(@PathVariable UUID uuid){
        return walletTypeStatusHistoryService.findAllByWalletTypeId(uuid)
                .stream()
                .map(mapper::toWalletTypeStatusHistoryOutputDto)
                .collect(Collectors.toList());
    }
}
