
#ifndef TRACH_MAINTABLEUI_H
#define TRACH_MAINTABLEUI_H


#include <QtWidgets/QWidget>
#include <QtWidgets/QFrame>
#include <QtGui/QPainter>
#include <Src/ServerConnection.h>
#include "CardUI.h"

class QDragEnterEvent;

class QDropEvent;

class MainTableUI : public QFrame
{
public:
    MainTableUI(QWidget *parent, ServerConnection *connection) : QFrame(parent)
    {
        this->connection = connection;
        setMinimumSize(600, 600);
        setFrameStyle(QFrame::Sunken | QFrame::StyledPanel);
        setAcceptDrops(true);
    }

    void paintEvent(QPaintEvent *e)
    {
        QPainter painter(this);
        painter.setPen(Qt::blue);
        painter.setFont(QFont("Arial", 30));
        painter.drawText(rect(), Qt::AlignCenter, "Aktualny Stół");
    }

    void setData(GameState *state)
    {
        clearCards();
        if (state->hasCardTree)
        {
            auto card = state->cardTree->playedCard->card;
            cardTree.emplace_back(new CardUI(this, *card));
            cardTree[cardTree.size() - 1]->addPlayerAvatar(state->cardTree->playedCard->whoPlayedId);
            int id =0;
            for (auto& child : state->cardTree->childrenNodes)
            {
                auto childCard = child.playedCard->card;
                cardTree.emplace_back(new CardUI(this, *childCard));
                cardTree[cardTree.size() - 1]->move(id*120, 200);
                cardTree[cardTree.size() - 1]->addPlayerAvatar(child.playedCard->whoPlayedId);
                id++;
            }
        }
    }

private:
    ServerConnection *connection;
    vector<CardUI *> activeCards;
    vector<CardUI *> cardTree;

    void drawSubTree(CardNode *node)
    {

    }

    void clearCards()
    {
        for (auto &card : cardTree)
        {
            delete card;
        }
        cardTree.clear();
    }

protected:

    void dragEnterEvent(QDragEnterEvent *event) override;

    void dragMoveEvent(QDragMoveEvent *event) override;

    void dropEvent(QDropEvent *event) override;
};


#endif //TRACH_MAINTABLEUI_H
