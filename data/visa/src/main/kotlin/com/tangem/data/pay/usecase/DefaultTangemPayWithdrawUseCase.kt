package com.tangem.data.pay.usecase

import arrow.core.Either
import com.tangem.core.error.UniversalError
import com.tangem.domain.models.wallet.UserWalletId
import com.tangem.domain.pay.repository.TangemPaySwapRepository
import com.tangem.domain.tangempay.TangemPayWithdrawUseCase
import javax.inject.Inject

internal class DefaultTangemPayWithdrawUseCase @Inject constructor(
    private val repository: TangemPaySwapRepository,
) : TangemPayWithdrawUseCase {

    override suspend fun invoke(
        userWalletId: UserWalletId,
        amountInCents: String,
        receiverCexAddress: String,
    ): Either<UniversalError, Unit> {
        return repository.withdraw(
            userWalletId = userWalletId,
            amountInCents = amountInCents,
            receiverAddress = receiverCexAddress,
        )
    }
}