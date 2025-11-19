package com.tangem.data.staking

import arrow.core.Either
import arrow.core.raise.catch
import arrow.core.raise.either
import com.tangem.datasource.local.token.P2PEthPoolVaultsStore
import com.tangem.domain.staking.model.ethpool.P2PEthPoolNetwork
import com.tangem.domain.staking.model.stakekit.StakingError
import com.tangem.domain.staking.repositories.P2PEthPoolRepository
import com.tangem.domain.staking.repositories.StakingErrorResolver
import javax.inject.Inject

/**
 * Use case for fetching and storing P2P Ethereum pooled staking vaults
 * Should be called on app startup (similar to FetchStakingTokensUseCase for StakeKit)
 *
 * @property p2pRepository repository for P2P ETH Pool API
 * @property p2pEthPoolVaultsStore store for caching vaults
 * @property stakingErrorResolver error resolver
 */
class FetchP2PEthPoolVaultsUseCase @Inject constructor(
    private val p2pRepository: P2PEthPoolRepository,
    private val p2pEthPoolVaultsStore: P2PEthPoolVaultsStore,
    private val stakingErrorResolver: StakingErrorResolver,
) {
    suspend operator fun invoke(network: P2PEthPoolNetwork = P2PEthPoolNetwork.MAINNET): Either<StakingError, Unit> {
        return either {
            catch(
                block = {
                    val vaults = p2pRepository.getVaults(network).bind()
                    p2pEthPoolVaultsStore.store(vaults)
                },
                catch = { error ->
                    stakingErrorResolver.resolve(error)
                },
            )
        }
    }
}