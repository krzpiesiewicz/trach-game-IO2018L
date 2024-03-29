
#include <QtWidgets>

#include "Src/UI/CurrentTreeTableUI.h"
#include "HandUI.h"

CurrentTreeTableUI::CurrentTreeTableUI(QWidget *parent, ServerConnection* connection, GameState *gameState,
                                       int playerId, HandUI* handUI)
        : QFrame(parent)
{
    this->playerId = playerId;
    this->connection = connection;
    setMinimumSize(660, 660);
    setFrameStyle(QFrame::Sunken | QFrame::StyledPanel);
    setAcceptDrops(true);

    currentState = gameState;
    currentTree = new CurrentCardTreeUI(this, gameState, handUI);

}

void CurrentTreeTableUI::setData(GameState *gameState)
{
    currentState = gameState;
}

void CurrentTreeTableUI::dragEnterEvent(QDragEnterEvent *event)
{
    {
        if (event->mimeData()->hasFormat("cardFromHand"))
        {
            event->acceptProposedAction();
        } else
        {
            event->ignore();
        }
    }
}

void CurrentTreeTableUI::dragMoveEvent(QDragMoveEvent *event)
{
    if (event->mimeData()->hasFormat("cardFromHand"))
    {
        event->acceptProposedAction();
    } else
    {
        event->ignore();
    }
}

void CurrentTreeTableUI::dropEvent(QDropEvent *event)
{
    if (event->mimeData()->hasFormat("cardFromHand"))
    {
        QByteArray itemData = event->mimeData()->data("cardFromHand");
        QDataStream dataStream(&itemData, QIODevice::ReadOnly);

        QPixmap pixmap;
        QPoint offset;
        int cardId;
        dataStream >> pixmap >> offset >> cardId;

        auto position = event->pos() - offset;
        if (currentTree->canCardBePlaced(position))
        {
            auto player = currentState->findPlayerById(playerId);
            auto card = player->findCardById(cardId);
            currentTree->addCard(position, *card);
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

void CurrentTreeTableUI::mousePressEvent(QMouseEvent *event)
{
    QLabel *childImage = dynamic_cast<QLabel *>(childAt(event->pos()));

    if (!childImage)
    {
        return;
    }
    auto *child = dynamic_cast<CurrentCardTreeUI *>(childImage->parentWidget()->parentWidget());

    QPixmap pixmap = *childImage->pixmap();

    QByteArray itemData;
    QDataStream dataStream(&itemData, QIODevice::WriteOnly);
    long long addrRaw = reinterpret_cast<long long>(child->getTree());

    dataStream << QPoint(event->pos() - child->pos()) << addrRaw;

    QMimeData *mimeData = new QMimeData;
    mimeData->setData("cardTreeToPlay", itemData);

    QDrag *drag = new QDrag(this);
    drag->setMimeData(mimeData);
    drag->setPixmap(pixmap);
    drag->setHotSpot(event->pos() - childImage->parentWidget()->pos());

    child->hide();

    auto action = drag->exec(Qt::CopyAction | Qt::MoveAction, Qt::CopyAction);
    if (action == Qt::MoveAction )
    {
        child->hide();
    } else if (action == Qt::CopyAction)
    {
        child->show();
        child->clearTree();
    } else {
        child->show();
    }

}