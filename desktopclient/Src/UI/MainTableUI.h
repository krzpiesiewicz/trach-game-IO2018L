
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
        QString targetString = "";
        if (state -> hasCardTree)
        {
            auto* card = state -> cardTree ->playedCard;
            if (card -> targetPlayer)
            {
                auto id = card ->targetId;
                auto* player = state->findPlayerById(id);
                targetString = QString::fromStdString("(Celem jest " + player ->name + ")\n");
            }
        }
        painter.drawText(rect(), Qt::AlignCenter, "Aktualny Stół\n" + targetString);

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
        this->state = state;
        clearCards();
        if (state->hasCardTree)
        {
            QPoint cardSize(120, 200);
            QPoint cardCenter = cardSize / 2.0f;
            QPoint offset(10, 10);

            auto card = state->cardTree->playedCard->card;
            cardTree.emplace_back(new CardUI(this, *card));

            int id =0;

            for (auto& child : state->cardTree->childrenNodes)
            {
                auto width = getSubTreeWidth(child);
                id+= width;
            }
            auto totalWidth = id;
            cardTree[0]->move(offset + QPoint( 62.5 * std::max(0,(totalWidth - 1)), 0));
            id = 0;

            for (auto& child : state->cardTree->childrenNodes)
            {
                auto width = getSubTreeWidth(child);
                auto cardPosition = offset + QPoint(125 * id + 62.5 * (width - 1), cardSize.y());
                addSubTree(child, cardPosition - QPoint( 62.5 * (width - 1), 0), cardPosition + cardCenter );
                edges.emplace_back(cardPosition + cardCenter, cardCenter + QPoint( 62.5 * std::max(0,(totalWidth - 1)), 0));
                id += width;
            }
        }
    }

private:
    GameState* state;
    ServerConnection *connection;
    std::vector<CardUI *> activeCards;
    std::vector<CardUI *> cardTree;
    std::vector<std::pair<QPoint,QPoint>>edges;

    int getSubTreeWidth(CardNode& node)
    {
        int sumOfWidths = 0;
        for (auto& child : node.childrenNodes)
        {
            sumOfWidths += getSubTreeWidth(child);
        }
        return std::max(1, sumOfWidths);
    }

    void addSubTree(CardNode& node, QPoint position, QPoint previousCardCenter)
    {
        QPoint cardSize(120, 200);
        QPoint cardCenter = cardSize / 2.0f;

        auto width = getSubTreeWidth(node);
        cardTree.emplace_back(new CardUI(this, *node.playedCard->card));
        cardTree[cardTree.size() - 1]->move(position + QPoint( 62.5 * (width - 1), 0));

        int id =0;

        for (auto& child : node . childrenNodes)
        {
            width = getSubTreeWidth(child);
            auto cardPosition = position + QPoint(125 * id + 62.5 * (width - 1), cardSize.y());
            addSubTree(child, cardPosition - QPoint( 62.5 * (width - 1), 0), cardPosition + cardCenter);
            edges.emplace_back(cardPosition + cardCenter, previousCardCenter);
            id += width;
        }
    }

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
