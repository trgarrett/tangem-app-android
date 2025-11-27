package com.tangem.data.pay.repository

import arrow.core.Either
import com.tangem.core.error.UniversalError
import com.tangem.datasource.api.pay.TangemPayApi
import com.tangem.datasource.api.pay.models.request.WithdrawDataRequest
import com.tangem.datasource.api.pay.models.request.WithdrawRequest
import com.tangem.domain.common.wallets.UserWalletsListRepository
import com.tangem.domain.models.wallet.UserWallet
import com.tangem.domain.models.wallet.UserWalletId
import com.tangem.domain.pay.datasource.TangemPayAuthDataSource
import com.tangem.domain.pay.repository.TangemPaySwapRepository
import com.tangem.domain.visa.error.VisaApiError
import com.tangem.domain.wallets.legacy.UserWalletsListManager
import com.tangem.features.hotwallet.HotWalletFeatureToggles
import javax.inject.Inject

internal class DefaultTangemPaySwapRepository @Inject constructor(
    private val tangemPayApi: TangemPayApi,
    private val requestHelper: TangemPayRequestPerformer,
    private val authDataSource: TangemPayAuthDataSource,
    private val userWalletsListManager: UserWalletsListManager,
    private val userWalletsListRepository: UserWalletsListRepository,
    private val hotWalletFeatureToggles: HotWalletFeatureToggles,
) : TangemPaySwapRepository {

    override suspend fun withdraw(
        userWalletId: UserWalletId,
        receiverAddress: String,
        amountInCents: String,
    ): Either<UniversalError, Unit> {
        return requestHelper.makeSafeRequest(userWalletId) { authHeader ->
            val request = WithdrawDataRequest(amountInCents = amountInCents, recipientAddress = receiverAddress)
            tangemPayApi.getWithdrawData(authHeader = authHeader, body = request)
        }.map { data ->
            val result = data.result
            if (result == null) return Either.Left(VisaApiError.WithdrawalDataError)
            val signature = authDataSource.getWithdrawalSignature(cardId = getCardId(userWalletId), hash = result.hash)
                .getOrNull()
            if (signature == null) return Either.Left(VisaApiError.SignWithdrawError)

            requestHelper.makeSafeRequest(userWalletId) { authHeader ->
                val request = WithdrawRequest(
                    amountInCents = amountInCents,
                    recipientAddress = receiverAddress,
                    adminSalt = result.salt,
                    senderAddress = result.senderAddress,
                    adminSignature = signature,
                )
                tangemPayApi.withdraw(authHeader = authHeader, body = request)
            }
                .mapLeft { return Either.Left(VisaApiError.WithdrawError) }
                .map { response -> if (response.result == null) return Either.Left(VisaApiError.WithdrawError) }
        }
    }

    private fun getCardId(userWalletId: UserWalletId): String {
        val userWallet = if (hotWalletFeatureToggles.isHotWalletEnabled) {
            userWalletsListRepository.userWallets.value?.firstOrNull { it.walletId == userWalletId }
        } else {
            userWalletsListManager.userWalletsSync.firstOrNull { it.walletId == userWalletId }
        } ?: error("No User Wallet found")
        return if (userWallet is UserWallet.Cold) {
            userWallet.cardId
        } else {
            ""
        }
    }
}