
#ifndef TRACH_CURRENTCARDTREEUI_H
#define TRACH_CURRENTCARDTREEUI_H


#include <Src/Core/CardTreeInternalNode.h>
#include "CardUI.h"
#include "HandUI.h"

class CurrentCardTreeUI : public QWidget
{
public:
    CurrentCardTreeUI(QWidget *parent, GameState *gameState, HandUI *handUI) : QWidget(parent)
    {
        this->handUI = handUI;
        currentState = gameState;
        hasCard = false;
        setMinimumSize(660,660);
    }

    bool canCardBePlaced(QPoint position)
    {
        return !hasCard;
    }

    void addCard(QPoint position, Card card)
    {
        hasCard = true;
        cards.emplace_back(new CardUI(this, card));
        cards[cards.size() - 1]->show();
        cards[cards.size() - 1]->move(200, 50);
    }

    CardTreeInternalNode* getTree()
    {
        auto* tree = new CardTreeInternalNode;
        tree->cardId = cards[0]->card.id;
        return tree;
    }

    void clearTree()
    {
        for (auto card : cards)
        {
            delete card;
        }
        hasCard = false;
        cards.clear();
        handUI-> showAllCards();
    }

private:
    GameState *currentState;
    bool hasCard;
    vector<CardUI *> cards;
    HandUI* handUI;
};


#endif //TRACH_CURRENTCARDTREEUI_H
