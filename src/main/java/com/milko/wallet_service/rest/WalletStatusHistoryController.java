package com.milko.wallet_service.rest;

import com.milko.wallet_service.dto.input.WalletRequestInputDto;
import com.milko.wallet_service.dto.output.WalletStatusHistoryOutputDto;
import com.milko.wallet_service.mapper.WalletStatusHistoryMapper;
import com.milko.wallet_service.service.WalletStatusHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/wallet_status_history")
public class WalletStatusHistoryController {
    private final WalletStatusHistoryService walletStatusHistoryService;

    @GetMapping
    public List <WalletStatusHistoryOutputDto> findAllByWalletId(@RequestBody WalletRequestInputDto walletRequestInputDto){
        return walletStatusHistoryService.findAllByWalletId(walletRequestInputDto.getWalletId(), walletRequestInputDto.getProfileId());
    }
}
