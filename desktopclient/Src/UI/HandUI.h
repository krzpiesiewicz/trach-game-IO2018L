
#ifndef TRACH_HANDUI_H
#define TRACH_HANDUI_H


#include <QtWidgets/QWidget>
#include <Src/Core/Card.h>
#include "CardUI.h"

class QDragEnterEvent;

class QDropEvent;

class HandUI : public QFrame
{


public:
    HandUI(QWidget *parent, vector<Card> cards) : QFrame(parent)
    {
        sort(cards.begin(), cards.end(), [](Card &a, Card &b) { return a.id < b.id; });
        setMinimumWidth(cards.size() * 120);
        setMinimumHeight(200);
        setAcceptDrops(true);
        setData(cards);
    }

    void setData(vector<Card> cards)
    {
        sort(cards.begin(), cards.end(), [](Card &a, Card &b) { return a.id < b.id; });

        bool isDirty = false;

        if (cardsUI.size() != cards.size())
        {
            isDirty = true;
        } else
        {
            for (int i = 0; i < (int)cardsUI.size(); i++)
            {
                if (cardsUI[i]->card.id != cards[i].id)
                {
                    isDirty = true;
                }
            }
        }
        if (!isDirty) { return; }

        clearCards();
        for (auto &card: cards)
        {
            cardsUI.emplace_back(new CardUI(this, card));
        }
        for (int i = 0; i < (int) cardsUI.size(); i++)
        {
            cardsUI[i]->move(i * 120, 0);
            cardsUI[i]->show();
            cardsUI[i]->setAttribute(Qt::WA_DeleteOnClose);
        }

    }

    void showAllCards()
    {
        for (auto& card: cardsUI)
        {
            card -> show();
        }
    }

protected:
    void mousePressEvent(QMouseEvent *event) override;

private:

    void clearCards()
    {
        for (auto& card: cardsUI)
        {
            delete card;
        }
        cardsUI.clear();
    }

    vector<CardUI *> cardsUI;
};


#endif //TRACH_HANDUI_H
