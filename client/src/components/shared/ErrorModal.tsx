// src/Modal.js

import * as React from 'react';
import CloseIcon from '@mui/icons-material/Close';
import '@/styles/shared/ErrorModal.scss';

interface ErrorModalProps {
  isOpen: boolean;
  closeModal: () => void;
  msg: string;
}


const ErrorModal = (props: ErrorModalProps) => {
  if (!props.isOpen) return null;

  return (
    <div className="modal-overlay" onClick={props.closeModal}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h3>Error</h3>
          {/* Close icon */}
          <CloseIcon className="close-icon" onClick={props.closeModal} />
        </div>
        <p>{props.msg}</p>
      </div>
    </div>
  );
};

export default ErrorModal;
