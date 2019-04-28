
#include <QtGui/QMouseEvent>
#include <QtGui/QDrag>
#include <QtGui/QPainter>
#include <QtWidgets>
#include "HandUI.h"


void HandUI::mousePressEvent(QMouseEvent *event)
{
    QLabel *childImage = dynamic_cast<QLabel *>(childAt(event->pos()));

    if (!childImage)
    {
        return;
    }
    auto *child = dynamic_cast<CardUI *>(childImage->parentWidget());

    cout << "mousePressEvent\n";

    QPixmap pixmap = *childImage->pixmap();

    QByteArray itemData;
    QDataStream dataStream(&itemData, QIODevice::WriteOnly);
    dataStream << pixmap << QPoint(event->pos() - child->pos()) << child->card.id;

    QMimeData *mimeData = new QMimeData;
    mimeData->setData("cardFromHand", itemData);

    QDrag *drag = new QDrag(this);
    drag->setMimeData(mimeData);
    drag->setPixmap(pixmap);
    drag->setHotSpot(event->pos() - child->pos());

    child->hide();

    auto action = drag->exec(Qt::CopyAction | Qt::MoveAction, Qt::CopyAction);
    if (action == Qt::MoveAction )
    {
        child->hide();
    } else if (action == Qt::CopyAction)
    {
        child->hide();
    } else {
        child->show();
    }
}

