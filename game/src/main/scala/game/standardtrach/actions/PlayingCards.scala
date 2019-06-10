package game.standardtrach.actions

import game.core._
import game.core.actions._

/**
 * PlayingCards is a player's action of attaching card subtree to an existing tree or adding a new card tree.
 * If the @cardNode is not attached then @state = @initialState.
 * 
 * There is assumption that objects @playedCardNode and @player are up-to-date with @initialState.
 */
class PlayingCards(playedCardNode: CardNode, player: Player)(implicit initialState: GameState) extends Action {
  
  {
    if (initialState.roundsManager.isBeginingOfTheRound && initialState.roundsManager.currentPlayer != player)
      throw new Exception(s"An action from the player of id=${player.id} is not expected.")
    if (!PlayingCards.verifyPlayedCards(playedCardNode, player))
      throw new Exception("All cards in played card node should be played and owned by a player who tries to attach the card node.")
  }

  /**
   * New game state with @playedCardNode attached. An exception is thrown if the @playedCardNode cannot be attached.
   */
  val state: GameState = {
    val (treeId, newTrees) = playedCardNode match {
      case newTree: TreeWithCards =>
        // if the @playedCardNode is a root node (tree with cards) then it should be added as a new tree
        val treeId = if (initialState.cardTrees.trees.keySet.isEmpty) 1 else initialState.cardTrees.trees.keySet.max + 1
        (treeId, initialState.cardTrees.trees + (treeId -> newTree))
      case _: CardInnerNode =>
        val (treeId, newTree) = Table.findTreeAndAttach(playedCardNode, initialState.cardTrees.trees.toStream)
        /* throws an exception if the proper tree not found */
        
        // Replace the found tree with @newTree
        (treeId, initialState.cardTrees.trees + (treeId -> newTree))
    }
    val stateWithPlayerCardsRemoved = playedCardNode.playedCards.foldLeft(initialState) {
      (state, pc) => new CardRemovalFromPlayersHand(pc.player, pc.card)(state).state
    }
    val stateWithCardNodeAdded = stateWithPlayerCardsRemoved transformed {
      case cardTrees: CardTrees => CardTrees(newTrees)
    }

    AllCardTreesEvaluation(stateWithCardNodeAdded, Some((treeId, playedCardNode))) /* throws an exception
      * if one of nodes of @cn subtree
      * cannot be evaluated to an action/transformer or it is not applicable. */

    stateWithCardNodeAdded transformed {
      case roundManager: RoundsManager => roundManager.roundStarted
    }
  }
}

private object PlayingCards {
  /**
   * Checks if all played cards are played by one player who has all of them in his hand.
   */
  def verifyPlayedCards(playedCardNode: CardNode, player: Player)(implicit state: GameState): Boolean = {
    
    val _player = state.player(player)

    /**
     * Recursively checks if all played cards from @cardNode are played by @player.
     */
    def verifyWhoPlayed(cardNode: CardNode): Boolean = _player == cardNode.playedCard.player &&
      cardNode.children.forall(verifyWhoPlayed(_))

    /**
     * Returns all played cards from subtree of @cardNode.
     */
    def getCards(cardNode: CardNode): Seq[PlayedCard[_]] =
      cardNode.children.foldLeft[Seq[PlayedCard[_]]](Seq(cardNode.playedCard)) {
        case (seq, child) => seq ++ getCards(child)
      }

    verifyWhoPlayed(playedCardNode) &&
      getCards(playedCardNode).forall(pc => pc.player.owns(pc.card.asInstanceOf[Card]))
  }
}
