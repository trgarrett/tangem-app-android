package com.tangem.domain.tangempay

import arrow.core.Either
import com.tangem.core.error.UniversalError
import com.tangem.domain.models.wallet.UserWalletId

interface TangemPayWithdrawUseCase {

    suspend operator fun invoke(
        userWalletId: UserWalletId,
        amountInCents: String,
        receiverCexAddress: String,
    ): Either<UniversalError, Unit>
}