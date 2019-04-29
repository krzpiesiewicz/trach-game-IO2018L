#include <QtWidgets>
#include <Src/Core/CardTreeInternalNode.h>
#include <iostream>
#include "MainTableUI.h"

void MainTableUI::dragEnterEvent(QDragEnterEvent *event)
{
    if (event->mimeData()->hasFormat("cardTreeToPlay"))
    {
        event->acceptProposedAction();
    } else
    {
        event->ignore();
    }
}

void MainTableUI::dragMoveEvent(QDragMoveEvent *event)
{
    if (event->mimeData()->hasFormat("cardTreeToPlay"))
    {
        event->acceptProposedAction();
    } else
    {
        event->ignore();
    }
}

void MainTableUI::dropEvent(QDropEvent *event)
{
    cout << "Drop table" << "\n";
    if (event->mimeData()->hasFormat("cardTreeToPlay"))
    {

        QByteArray itemData = event->mimeData()->data("cardTreeToPlay");
        QDataStream dataStream(&itemData, QIODevice::ReadOnly);

        QPoint offset;
        long long addrRaw;
        dataStream >> offset >> addrRaw;

        CardTreeInternalNode *tree;
        tree = reinterpret_cast<CardTreeInternalNode *>(addrRaw);

        auto position = event->pos();
        Card *foundCard = nullptr;


        for (auto &card : cardTree)
        {
            if (card->geometry().contains(position))
            {
                foundCard = &(card->card);
            }
        }

        if (foundCard != nullptr)
        {
            cout <<foundCard->id << "\n";
            connection ->playCardTreeAtCardTree(tree, foundCard->id);
            event->acceptProposedAction();
        } else
        {
            event->ignore();
        }

    } else
    {
        event->ignore();
    }
}