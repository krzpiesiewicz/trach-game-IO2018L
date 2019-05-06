
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
        setMinimumSize(660, 660);
        setFrameStyle(QFrame::Sunken | QFrame::StyledPanel);
        setAcceptDrops(true);
    }

    void paintEvent(QPaintEvent *e)
    {
        static const QPointF points[4] = {
                QPointF(5.0, 5.0),
                QPointF(5.0, 655.0),
                QPointF(655.0, 655.0),
                QPointF(655.0, 5.0)
        };

        QPainter painter(this);

        QPen pen;
        pen.setWidth(2);
        painter.setPen(pen);
        painter.drawPolygon(points, 4);
        painter.setFont(QFont("Arial", 30));
        painter.drawText(rect(), Qt::AlignCenter, "Aktualny Stół");

        QPen background;
        background.setWidth(14);
        painter.setPen(background);

        for (auto& edge : edges)
        {
            painter.drawLine(edge.first, edge.second);
        }
    }

    void setData(GameState *state)
    {
        clearCards();
        if (state->hasCardTree)
        {
            QPoint cardSize(120, 200);
            QPoint cardCenter = cardSize / 2.0f;
            QPoint offset(10, 10);

            auto card = state->cardTree->playedCard->card;
            cardTree.emplace_back(new CardUI(this, *card));
            cardTree[cardTree.size() - 1]->move(offset);
            int id =0;

            for (auto& child : state->cardTree->childrenNodes)
            {
                auto cardPosition = offset + QPoint(125 * id, cardSize.y());
                addSubTree(child, cardPosition, cardCenter);
                edges.emplace_back(cardPosition + cardCenter, cardCenter);
                id += getSubTreeWidth(child);
            }
        }
    }

    void addSubTree(CardNode& node, QPoint position, QPoint previousCardCenter)
    {
        QPoint cardSize(120, 200);
        QPoint cardCenter = cardSize / 2.0f;

        cardTree.emplace_back(new CardUI(this, *node.playedCard->card));
        cardTree[cardTree.size() - 1]->move(position);
        int id =0;

        for (auto& child : node . childrenNodes)
        {
            auto cardPosition = QPoint(125 * id, cardSize.y());
            addSubTree(child, cardPosition, position + cardCenter);
            edges.emplace_back(cardPosition + cardCenter, previousCardCenter);
            id += getSubTreeWidth(child);
        }
    }

    int getSubTreeWidth(CardNode& node)
    {
        int sumOfWidths = 0;
        for (auto& child : node.childrenNodes)
        {
            sumOfWidths += getSubTreeWidth(child);
        }
        return max(1, sumOfWidths);
    }

private:
    ServerConnection *connection;
    vector<CardUI *> activeCards;
    vector<CardUI *> cardTree;
    vector<pair<QPoint,QPoint>>edges;

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
        edges.clear();
    }

protected:

    void dragEnterEvent(QDragEnterEvent *event) override;

    void dragMoveEvent(QDragMoveEvent *event) override;

    void dropEvent(QDropEvent *event) override;
};


#endif //TRACH_MAINTABLEUI_H
