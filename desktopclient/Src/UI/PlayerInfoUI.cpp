
#include <QtWidgets>
#include <Src/Core/CardTreeInternalNode.h>

#include "PlayerInfoUI.h"

void PlayerInfoUI::dragEnterEvent(QDragEnterEvent *event)
{
    if (event->mimeData()->hasFormat("cardTreeToPlay"))
    {
        event->acceptProposedAction();
    } else
    {
        event->ignore();
    }
}

void PlayerInfoUI::dragMoveEvent(QDragMoveEvent *event)
{
    if (event->mimeData()->hasFormat("cardTreeToPlay"))
    {
        event->acceptProposedAction();
    } else
    {
        event->ignore();
    }
}

void PlayerInfoUI::dropEvent(QDropEvent *event)
{
    cout << "Drop table" << "\n";
    if (event->mimeData()->hasFormat("cardTreeToPlay"))
    {

        QByteArray itemData = event->mimeData()->data("cardTreeToPlay");
        QDataStream dataStream(&itemData, QIODevice::ReadOnly);

        QPoint offset;
        long long addrRaw;
        dataStream >> offset >> addrRaw;
        cout <<"received  "<<addrRaw<<"\n";
        CardTreeInternalNode* tree;
        tree = reinterpret_cast<CardTreeInternalNode*>(addrRaw);
        cout <<"played card id is "<<tree->cardId<<"\n";
        connection ->playCardAtPlayer(tree, playerId);


        event->acceptProposedAction();

    } else
    {
        event->ignore();
    }
}