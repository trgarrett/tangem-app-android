package com.tangem.domain.pay.repository

import arrow.core.Either
import com.tangem.core.error.UniversalError
import com.tangem.domain.models.wallet.UserWalletId

interface TangemPaySwapRepository {

    suspend fun withdraw(
        userWalletId: UserWalletId,
        receiverAddress: String,
        amountInCents: String,
    ): Either<UniversalError, Unit>
}